package BlockStackTest;
import java.util.Random;

public class BlockPlan {
	
	private int[][] planData;
	private int[] bTypes;
	private int bNum;
	private int planFitness = 0;
	
	public BlockPlan(int blockNum, int[] blockTypes){
		//configure containers
		planData = new int[blockNum][2];
		bTypes = blockTypes;
		bNum = blockNum;
	}
	
	public void createRandomPlan(){
		//Shuffle block Type order
		shuffleArray();
		//allocate random positions
		for(int i = 0; i < bNum; i++){
			planData[i][0] = bTypes[i]; 
			planData[i][1] = (int)( Math.random() * 99 ); //generates random int 0-99 for x loc
		} 
	}
	
	// Get/Set/Print Methods
	
	public int[][] getPlan(){
		return planData;
	}
	
	public void setPlan(int[][] newPlan){
		planData = newPlan;
	}
	
	public void printPlan(){
		//prints out the plan
		for(int i = 0; i < bNum; i++){
			System.out.println(planData[i][0] + ", X=" + planData[i][1]);
		}
	}
	
	public int getFitness(){
		return planFitness;
	}
	
	public void setFitness(int fitness){
		planFitness = fitness;
	}
	
	//private methods
	
	private void shuffleArray(){
		//shuffles a given array
		int[] ranBlockTypes = bTypes;
	    Random rnd = new Random();
	    for (int i = bTypes.length - 1; i > 0; i--)
	    {
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      int a = bTypes[index];
	      bTypes[index] = bTypes[i];
	      bTypes[i] = a;
	    }
	    bTypes = ranBlockTypes;
	  }

}
