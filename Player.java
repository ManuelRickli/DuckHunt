import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Player {

    private int round;
    private int tries;

    private int bestBirdIndex;
    private double bestLogProb;

    private int[] speciesCount;

    private List<HMM> shootModels;
    private List<List<HMM>> speciesModels;
    private List<List<Integer>> speciesObservations;
    private List<HMM> guessModelsMean;
    private List<HMM> guessConcatenateObservationsModels;

    private HMM bestModel;
    private Bird bestBird;

    Player() {

        this.round = -1;
        this.tries = -1;
        this.speciesCount = new int[Constants.COUNT_SPECIES];

        this.shootModels = new ArrayList<>();
        this.speciesModels = new ArrayList<>();
        this.guessModelsMean = new ArrayList<>();
        this.guessConcatenateObservationsModels = new ArrayList<>();
        this.speciesObservations = new ArrayList<>();

        for (int i = 0; i < Constants.COUNT_SPECIES; i++) {
            guessModelsMean.add(new HMM());
            speciesModels.add(new ArrayList<>());
            speciesObservations.add(new ArrayList<>());
            guessConcatenateObservationsModels.add(new HMM());
        }
    }

    private void newRound(GameState pState) {

        tries = -1;
        round = pState.getRound();
        shootModels = new ArrayList<>();
        bestBirdIndex = -1;
        bestLogProb = Double.NEGATIVE_INFINITY;
        bestBird = null;
        bestModel = null;

        for (int i = 0; i < pState.getNumBirds(); i++) {
            shootModels.add(new HMM());
        }
    }

    private boolean isTimeToShoot(GameState gameState) {

        return ++tries >= 98 - gameState.getNumBirds();
    }

    Action shoot(GameState gameState, Deadline pDue) {

        int currentRound = gameState.getRound();

        if (round != currentRound) {
            newRound(gameState);
            return dontShoot();
        }

        if (!isTimeToShoot(gameState)) {
            return dontShoot();
        }

        int bestEmissionIndex;

        double bestEmissionProb;

        for (int i = 0; i < gameState.getNumBirds(); ++i) {

            Bird bird = gameState.getBird(i);

            if (bird.isDead()) {
                continue;
            }

            bestLogProb = trainModel(i, bestLogProb, bird);
        }

        if (bestBird == null) {
            return dontShoot();
        }

        Vector prediction = bestModel.predictNextEmissionDistribution(bestBird);
        bestEmissionIndex = prediction.getMaxIndex();
        bestEmissionProb = prediction.array[bestEmissionIndex];

        int speciesGuess = guessBird(bestBird);
        double guessBlackBirdConcatenate = guessConcatenateObservationsModels.get(Constants.SPECIES_BLACK_STORK).estimateBird(bestBird);

        if (guessBlackBird(speciesGuess, guessBlackBirdConcatenate)) {
            return dontShoot();
        }

        return bestEmissionProb > 0.7 ? new Action(bestBirdIndex, bestEmissionIndex): dontShoot();
    }

    private double trainModel(int birdIndex, double bestLogProb, Bird bird) {

        HMM birdModel = new HMM();
        double logProb = birdModel.trainModel(bird);
        shootModels.set(birdIndex, birdModel);

        if (logProb < bestLogProb) {
            return bestLogProb;
        }

        bestModel = birdModel;
        bestBird = bird;
        bestBirdIndex = birdIndex;

        return logProb;
    }

    private boolean guessBlackBird(int speciesGuess, double guessBlackBirdConcatenate) {

        return Math.log(guessBlackBirdConcatenate) > -110 ||
                speciesGuess == Constants.SPECIES_BLACK_STORK ||
                speciesGuess == Constants.SPECIES_UNKNOWN;
    }

    int[] guess(GameState pState, Deadline pDue) {

        if (round == 0) {
            return randomGuess(pState);
        }

        int[] guesses = new int[pState.getNumBirds()];

        for (int i = 0; i < pState.getNumBirds(); i++) {
            guesses[i] = guessBird(pState.getBird(i));
        }

        return guesses;
    }

    private int guessBird(Bird currentBird) {

        int speciesGuess = Constants.SPECIES_UNKNOWN;
        double maxEstimation = 0.0;

        for (int j = 0; j < Constants.COUNT_SPECIES; j++) {

            if (speciesCount[j] == 0) {
                continue;
            }

            double estimation11 = guessModelsMean.get(j).estimateBird(currentBird);
            double estimation22 = guessConcatenateObservationsModels.get(j).estimateBird(currentBird);
            double estimation33 = guessUsingMaxAPassProb(j, currentBird);
            double estimation = estimation11 > estimation22 ? estimation11 : estimation22;

            estimation = estimation33 > estimation ? estimation33 : estimation;

            if (estimation > maxEstimation) {
                maxEstimation = estimation;
                speciesGuess = j;
            }
        }

        return speciesGuess;
    }

    private double guessUsingMaxAPassProb(int species, Bird currentBird) {

        double maxProb = Double.NEGATIVE_INFINITY;

        List<HMM> modelsOfSpecies = speciesModels.get(species);

        for (HMM model : modelsOfSpecies) {

            double modelEstimationProb = model.estimateBird(currentBird);

            if (modelEstimationProb <= maxProb) {
                continue;
            }

            maxProb = modelEstimationProb;
        }
        return maxProb;
    }

    void hit(GameState pState, int pBird, Deadline pDue) {

    }

    void reveal(GameState pState, int[] pSpecies, Deadline pDue) {

        for (int i = 0; i < pSpecies.length; i++) {

            int specieIndex = pSpecies[i];

            if (pSpecies[i] == Constants.SPECIES_UNKNOWN) {
                continue;
            }

            speciesObservations.get(specieIndex).addAll(observe(pState.getBird(i)));
            speciesModels.get(specieIndex).add(shootModels.get(i));
            speciesCount[specieIndex]++;
        }

        for (int i = 0; i < speciesModels.size(); i++) {

            if (speciesModels.get(i).isEmpty()) {
                continue;
            }

            createGuessMergeMeanModels(i);
            createGuessConcatenateObservationModels(i);
        }
    }

    private void createGuessMergeMeanModels(int birdSpecies) {

        List<HMM> hmmModelsToMerge = new ArrayList<>();
        hmmModelsToMerge.addAll(speciesModels.get(birdSpecies));
        hmmModelsToMerge.add(guessModelsMean.get(birdSpecies));

        guessModelsMean.set(birdSpecies, HMM.merge(hmmModelsToMerge));
    }

    private void createGuessConcatenateObservationModels(int birdSpecies) {

        List<Integer> speciesobservationsList = speciesObservations.get(birdSpecies);
        Integer[] speciesobservations = new Integer[speciesobservationsList.size()];

        for (int j = 0; j < speciesobservationsList.size(); j++) {
            speciesobservations[j] = speciesobservationsList.get(j);
        }

        HMM model = new HMM();
        model.trainModel(speciesobservations);

        guessConcatenateObservationsModels.set(birdSpecies, model);
    }

    private List<Integer> observe(Bird bird) {

        List<Integer> observationsList = new ArrayList<>();

        for (int i = 0; i < bird.getSeqLength(); i++) {

            if (bird.wasAlive(i)) {
                observationsList.add(bird.getObservation(i));
            }
        }

        Integer[] observations = new Integer[observationsList.size()];

        for (int j = 0; j < observations.length; j++) {
            observations[j] = observationsList.get(j);
        }

        return Arrays.asList(observations);
    }

    private Action dontShoot() {

        return new Action(-1, -1);
    }

    private int[] randomGuess(GameState pState) {

        int[] randomGuess = new int[pState.getNumBirds()];

        for (int i = 0; i < pState.getNumBirds(); ++i) {
            randomGuess[i] = Constants.SPECIES_PIGEON;
        }

        return randomGuess;
    }
}
