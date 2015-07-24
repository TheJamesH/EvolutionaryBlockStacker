#pragma strict
import System.IO;

var filePath : String;
var cube1 : GameObject;
var cube2 : GameObject;
var cube3 : GameObject;
var cube4 : GameObject;
var cube5 : GameObject;
private var spawn_position;
private var timer = 5.0;
private var cubeNum : int;
private var planNum : int;
private var curGenNum = 0;
private var curPlanNum = 0;
private var count = 1;
private var waitTime = 0.50;
private var fitness = 0;
private var prvFitness = 0;
private var topFitness = 0;
private var simLoop = false;
private var planLoop = true;
private var firstPass = true;
private var plans = new Array(); // I tried making this a 3d array, but it got too frustraiting
private var planFitnesses = new Array();

function spawn_cube () {
	spawn_position = Vector3(plans[((curPlanNum*cubeNum*2)+(count*2)-1)],80,0);
	switch(plans[((curPlanNum*cubeNum*2)+(count*2)-2)]) {
		case 0:
			var temp_spawn_cube1 = Instantiate(cube1, spawn_position, Quaternion.identity);
			break;
		case 1:
			var temp_spawn_cube2 = Instantiate(cube2, spawn_position, Quaternion.identity);
			break;
		case 2:
			var temp_spawn_cube3 = Instantiate(cube3, spawn_position, Quaternion.identity);
			break;
		case 3:
			var temp_spawn_cube4 = Instantiate(cube4, spawn_position, Quaternion.identity);
			break;
		case 4:
			var temp_spawn_cube5 = Instantiate(cube5, spawn_position, Quaternion.identity);
			break;
	}
}

function calc_fitness () {
	for(var block : GameObject in GameObject.FindGameObjectsWithTag("block")) { //add up all block locs
    	if(block.transform.position.y > 0){
    		fitness += (block.transform.position.y * block.transform.position.y); // log increase
    	}
    	else {
    		fitness -= (block.transform.position.y * block.transform.position.y);
    	}
    }
    if(fitness < 0){
    	fitness = 0;
    	Debug.Log("Fitness < 0, That wasn't very good!");
    }
}

function OnGUI(){
	GUI.Label(Rect(10,0,Screen.width,Screen.height),"Current Gen: #" + curGenNum);
	GUI.Label(Rect(10,15,Screen.width,Screen.height),"Current Plan: #" + (curPlanNum+1));
    GUI.Label(Rect(10,30,Screen.width,Screen.height),"Current Block: #" + (count-1));
    GUI.Label(Rect(500,0,Screen.width,Screen.height),"Highest Fitness = " + topFitness);
    GUI.Label(Rect(500,15,Screen.width,Screen.height),"Previous Fitness = " + prvFitness);
}

function ReadFile(filepathIncludingFileName : String) {
    var sr = new StreamReader(filepathIncludingFileName);
    var input = "";
    input = sr.ReadLine();
    if (input == null) { 
    	sr.Close();
    	return; 
    } //nothing in file
    if (parseInt(input) == 1) { 
    	sr.Close();
    	return; 
    } //file not in "plan" mode
    input = sr.ReadLine(); //read in sizes
    cubeNum = parseInt(input);
    input = sr.ReadLine();
    planNum = parseInt(input);
    plans = new Array(((planNum*cubeNum)+(cubeNum*2)-1));
    for (var i = 0; i < planNum; i++) {
    	for (var j = 1; j <= cubeNum; j++) {
       		input = sr.ReadLine();
       		plans[((i*cubeNum*2)+(j*2)-2)] = parseInt(input); //enter block type
       		input = sr.ReadLine();
       		plans[((i*cubeNum*2)+(j*2)-1)] = parseInt(input); //enter block location
       	}
    }
    sr.Close();
    planFitnesses = [];
    curGenNum++;
    curPlanNum = 0;
    simLoop = true;
    planLoop = true;
}

function WriteFile(filepathIncludingFileName : String)
{
	Debug.Log("Writng File");
    var sw : StreamWriter = new StreamWriter(filePath);
    sw.WriteLine("1");
    for (var k = 0; k < planFitnesses.length; k++){
    	sw.WriteLine(planFitnesses[k] + "");
    }
    Debug.Log("Done");
    sw.Flush();
    sw.Close();
    simLoop = false;
}

function Start () {

}

function Update () {
	OnGUI();
	timer += Time.deltaTime;
	if(!simLoop && timer > waitTime){
		ReadFile(filePath);
		timer = 0.0;
	} 
	else if(!planLoop){
		if((curPlanNum + 2) > planNum){
			planLoop = true;
			WriteFile(filePath);
		}
		else{ 
			curPlanNum++;
			planLoop = true;
		}
	}
	else if(timer > waitTime) {
		if(count <= cubeNum) { //spawn next cube
			//calc_fitness();
			spawn_cube();
			//Update CubeNumbs, reset timer
			count++;
			timer = 0.0;
			waitTime += 0.02;
		}
		else if(firstPass){
			waitTime = 3.0;
			timer = 0.0;
			firstPass = false;
		}
		else{
			waitTime = 0.50;
			calc_fitness();
			prvFitness = fitness;
			if(fitness > topFitness){
				Debug.Log("New Top Fitness! Gen#" + curGenNum + " Plan#" + (curPlanNum+1));
				topFitness = fitness;
			}
			planFitnesses.push(fitness);
			fitness = 0;
			for(var block : GameObject in GameObject.FindGameObjectsWithTag("block")) {
				Destroy(block);
			}
			planLoop = false;
			firstPass = true;
			count = 1;
		}
	}
}