
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BlockStack3D {
	
	//--------------Variables-------------
	private static String fileLoc = "C:\\Users\\Public\\Documents\\Unity Projects\\BoxStacker3D\\Assets\\plans.txt"; //The location of the text file that both programs read
	private static int blockNum = 20; //The number of blocks used
	private static int planNum = 20; //The number of plans per generation
	private static int perserveNum = 2; //The number of top plans to save for next generation
	private static int newRandomNum = 0; //The number of completely random plans to generate each generation
	private static int generationsNum = 100; //The number of generations
	private static int mutateOrderRate = 50; //(0-100%) percentage chance for a Order Mutation
	private static int mutateLocationRate = 90; //(0-100%) percentage chance for a Location Mutation
	
	private static int[] blockTypes = new int[blockNum];
	static BufferedWriter writer;
	static BufferedReader reader;
	
	public static void main(String[] args) throws IOException, InterruptedException{
		//-----------Sets up all block types for testing--------------
		for(int i = 0; i < blockNum; i++){
			blockTypes[i] = i%5; //This test splits between 5 block types
		}
		//------------------------------------------------------------
		//Start generation 1
		System.out.println("===Generation 0===");
		BlockPlan[] currentPlans = new BlockPlan[planNum] ;
		for(int i = 0; i < planNum; i++){
			currentPlans[i] =new BlockPlan(blockNum, blockTypes);
		}
		for(int i = 0; i < planNum; i++){
			currentPlans[i].createRandomPlan();
		}
		for(int i = 0; i < planNum; i++){
			System.out.println("---Random Plan " + (i+1) + "---");
			currentPlans[i].printPlan();
		}
		System.out.println("Running simulation");
		runSimulation(currentPlans);
		System.out.println("Simulation Complete");
		//Start Generation Loop.
		for(int gen = 1; gen < generationsNum; gen++){
			System.out.println("===Generation " + gen + "===");
			currentPlans = createNewGeneration(currentPlans);
			for(int i = 0; i < planNum; i++){
				System.out.println("---Plan " + (i + 1) + "---");
				currentPlans[i].printPlan();
			}
			System.out.println("Running simulation");
			runSimulation(currentPlans);
			System.out.println("Simulation Complete");
		}
	}
	
	public static void runSimulation(BlockPlan[] currentPlans) throws IOException, InterruptedException{
		//Write out current plans to shared text folder
		File file = new File(fileLoc);
		System.out.println("Writing");
		while (!file.exists()) {
		    try { 
		        Thread.sleep(100);
		    } catch (InterruptedException ie) { /* safe to ignore */ }
		}
		file.createNewFile();
		FileWriter f = new FileWriter(file);
		writer = new BufferedWriter(f);
		writer.write("0"); //0 = pre sim, 1 = post sim
		writer.newLine();
		writer.write(blockNum+"");
		writer.newLine();
		writer.write(planNum+"");
		writer.newLine();
		for(int i = 0; i < planNum; i++){
			 int[][] plandata = currentPlans[i].getPlan();
			 for(int j = 0; j < blockNum; j++){
				 writer.write(plandata[j][0]+"");
				 writer.newLine();
				 writer.write(plandata[j][1]+"");
				 writer.newLine();
				 writer.write(plandata[j][2]+"");
				 writer.newLine();
			 }
		}
		writer.close();
		//Wait for simulation to end
		System.out.println("Waiting");
		while(true){
			TimeUnit.SECONDS.sleep(1);
			FileReader r = new FileReader(file);
			reader = new BufferedReader(r);
			String input = reader.readLine();
			if(Integer.parseInt(input) == 1){
				break;
			}
			reader.close();
		}
		//Read in plan fitnesses
		System.out.println("Reading");
		for(int i = 0; i < planNum; i++){
			currentPlans[i].setFitness(Integer.parseInt(reader.readLine()));
		}
	}
		
    public static BlockPlan[] createNewGeneration(BlockPlan[] currentPlans){
    	BlockPlan[] newPlans = new BlockPlan[planNum];
    	int totalFitness = 0;
    	int fitness = 0;
    	//Sort plans by fitness
    	currentPlans = sort(currentPlans);
    	for(int i = 0; i < perserveNum; i++){ //Preserve top plans for next generation.
    		System.out.println("## Perserving plan " + (i+1) + " ##");
    		int[][] newPlanData = new int[blockNum][2];
    		for (int j = 0; j < blockNum; j++) {
    			newPlanData[j] = currentPlans[i].getPlan()[j].clone();
    		}
    		BlockPlan newPlan = new BlockPlan(blockNum, blockTypes);
    		newPlan.setPlan(newPlanData);
    		newPlans[i] = newPlan;
		}
    	for(int i = perserveNum; i < (perserveNum + newRandomNum); i++){ //Create new random plans
    		System.out.println("## Creating new random plan for " + (i+1)+ " ##");
    		newPlans[i] = new BlockPlan(blockNum, blockTypes);
    		newPlans[i].createRandomPlan();
    	}
    	for(int i = 0; i < planNum; i++){//tally fitness of all plans
			fitness = currentPlans[i].getFitness();
			totalFitness += fitness;
		}
    	for(int i = (perserveNum + newRandomNum); i < planNum; i++){ //fill remaining plans with augmented plans
    		BlockPlan planA;
    		int aNum = (int)( Math.random() * totalFitness );
    		int j = 0;
    		int k = 0;
    		for(k = 0; j < aNum; k++){
    			j += currentPlans[k].getFitness();
    		}
    		k--;
    		if(k < 0 || k >= currentPlans.length){
    			k = 0;
    		}
    		planA = currentPlans[k];
    		System.out.print("## Augmenting plan " + k + " for plan " + (i+1) + " ##");
    		BlockPlan planB = augmentPlan(planA);
    		newPlans[i] = planB;
    	}
		return newPlans;
    }
    
    public static BlockPlan augmentPlan(BlockPlan planA){
    	//Copy Plan A
    	int[][] planCData = new int[blockNum][2];
    	for (int i = 0; i < blockNum; i++) {
    		planCData[i] = planA.getPlan()[i].clone();
    	}
    	//Mutate order
    	int mutateOrderNum = (int)(Math.random() * 101);
    	if(mutateOrderNum >= (100 - mutateOrderRate)){
    		System.out.println("~Order Mutation~");
    		int numtochange = (int) ((int) ((Math.random() * (int) (.3 * (blockNum * blockNum))))/blockNum); //changes up to 30% of blocks
    		if(numtochange == 0){
    			numtochange = 1;
    		}
    		System.out.println("Changing " + numtochange);
    		for(int i = 0; i < numtochange; i++){
    			int blocktochangeA = (int)(Math.random() * blockNum);
    			int blocktochangeB = (int)(Math.random() * blockNum);
    			int[] temp = planCData[blocktochangeA];
    			planCData[blocktochangeA] = planCData[blocktochangeB];
    			planCData[blocktochangeB] = temp;
    			System.out.println(blocktochangeA + " swaps with " + blocktochangeB);
    		}
    	}
    	//Mutate locations
    	int mutateLocationNum = (int)(Math.random() * 101);
    	if(mutateLocationNum >= (100 - mutateLocationRate)){
    		System.out.println("~Location Mutation~");
    		int numtochange = (int) ((int) ((Math.random() * (int) (.3 * (blockNum * blockNum))))/blockNum); //changes up to 30% of blocks
    		if(numtochange == 0){
    			numtochange = 1;
    		}
    		System.out.println("Changing " + numtochange);
    		for(int i = 0; i < numtochange; i++){
    			//choose wich directions to move(0-40 = x, 40-80 = z, >80 = x and z);
    			int moveDirection = (int)(Math.random() * 101);
    			boolean moveX = false;
    			boolean moveZ = false;
    			if(moveDirection > 40){
    				moveZ = true;
    				if(moveDirection > 80){
    					moveX = true;
    				}
    			}
    			else{
    				moveX = true;
    			}
    			//chose wich type of move
    			if((int)(Math.random() * 101) > 30){ //blockShift
    				int blocktochange = (int)(Math.random() * blockNum);
    				if(moveX){
    					planCData[blocktochange][1] = (int) (planCData[blocktochange][1] + ((Math.random() * 20)-10));
    				}
    				if(moveZ){
    					planCData[blocktochange][1] = (int) (planCData[blocktochange][2] + ((Math.random() * 20)-10));
    				}
    			}
    			else{ //newranLoc
        			int blocktochange = (int)(Math.random() * blockNum);
        			if(moveX){
        				planCData[blocktochange][1] = (int)( Math.random() * 99 );
    				}
    				if(moveZ){
    					planCData[blocktochange][2] = (int)( Math.random() * 99 );
    				}
    			}
    		}
    	}
    	BlockPlan planC = new BlockPlan(blockNum, blockTypes);
    	planC.setPlan(planCData);
    	return planC;
    }
    
    //-----------------Quicksort methods--------------------------
    
    private static BlockPlan[] BlockPlans;
    private static int length;
    
    public static BlockPlan[] sort(BlockPlan[] inputArr) {
        
        if (inputArr == null || inputArr.length == 0) {
            return inputArr;
        }
        BlockPlans = inputArr;
        length = inputArr.length;
        quickSort(0, length - 1);
        BlockPlans = reversePlanArray(BlockPlans);
        return BlockPlans;
    }
 
    private static void quickSort(int lowerIndex, int higherIndex) {
        int i = lowerIndex;
        int j = higherIndex;
        // calculate pivot number
        int pivot = BlockPlans[lowerIndex+(higherIndex-lowerIndex)/2].getFitness();
        // Divide BlockPlans two arrays
        while (i <= j) {
            while (BlockPlans[i].getFitness() < pivot) {
                i++;
            }
            while (BlockPlans[j].getFitness() > pivot) {
                j--;
            }
            if (i <= j) {
                exchangeNumbers(i, j);
                //move index to next position on both sides
                i++;
                j--;
            }
        }
        // call quickSort() method recursively
        if (lowerIndex < j)
            quickSort(lowerIndex, j);
        if (i < higherIndex)
            quickSort(i, higherIndex);
    }
 
    private static void exchangeNumbers(int i, int j) {
    	BlockPlan temp = BlockPlans[i];
        BlockPlans[i] = BlockPlans[j];
        BlockPlans[j] = temp;
    }
    
    public static BlockPlan[] reversePlanArray(BlockPlan[] x) {
    	BlockPlan tmp;    
        for (int i = 0; i < x.length / 2; i++) {
            tmp = x[i];
            x[i] = x[x.length - 1 - i];
            x[x.length - 1 - i] = tmp;
        }
        return x;
    }
}
