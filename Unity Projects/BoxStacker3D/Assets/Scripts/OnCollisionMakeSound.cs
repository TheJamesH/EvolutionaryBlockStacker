
using UnityEngine;
using System.Collections;

public class OnCollisionMakeSound : MonoBehaviour {
	AudioSource asrc;
	public AudioClip clip;
	float[] pitches = new float[] { 0.84090378328F, 0.94387706945F, 1.05946F, 1.1224554916F, 1.25990633F, 1.4141887797F};

	// Use this for initialization
	void Start () {
		asrc = GetComponent(typeof(AudioSource)) as AudioSource;
	}
	
	// Update is called once per frame
	void Update () {
	
	}

	void OnCollisionEnter(Collision Col){
		if(asrc != null)
		{
			int pitchSelected = Random.Range(0, 6);
			asrc.pitch = pitches[pitchSelected];
			asrc.PlayOneShot(clip, 1);
		}
	}
}
