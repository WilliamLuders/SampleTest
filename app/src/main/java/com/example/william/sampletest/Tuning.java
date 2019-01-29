package com.example.william.sampletest;//import com.skeletonkey.capstone.skeletonkey.module.interfaces.AlgorithmConnection;

public class Tuning implements AlgorithmConnection {
	private double desiredFreq, desF11;
	private final double ratioF11= 1.59334; //ratio between 01-mode frequency and 11-mode frequency
	private double[] f11;
	private double maxFreq = 300, minFreq = 80;
	private double thetas[];
	private double[] tuningCoeffs8Lugs = new double[] {-0.00160080346698326,	0.0137496493638949,	-0.00708950032447878,	-0.00467329249232186,	0.00132351822650013,	-0.00467329249232186,	-0.00708950032447878,	0.0137496493638949};

	public void init() {
		//need to import numLugs from drum setup
		int numLugs = 8;
		thetas = new double[numLugs];
		f11 = new double[numLugs];

		//import max,min freqs and tuning sensitivity

	}
	public double getDesiredFreq() {
		return desiredFreq;
	}
	public double setDesiredFreq(double newDesiredFreq)	{
		if(newDesiredFreq>maxFreq)
			desiredFreq = maxFreq;
		else if (newDesiredFreq<minFreq)
			desiredFreq = minFreq;
		else
			desiredFreq=newDesiredFreq;

		desF11 = ratioF11 * desiredFreq;
		return desiredFreq;
	}

	public double getMinFreq() {
		return minFreq;
	}
	public void setMinFreq(double newMinFreq) {
		minFreq = newMinFreq;
	}

	public double getMaxFreq() {
		return maxFreq;
	}
	public void setMaxFreq(double newMaxFreq) {
		maxFreq = newMaxFreq;
	}

//	public void setF11(int lugNumber, double frequency ){
//		if (lugNumber>8||lugNumber<1) {
//			throw new IllegalStateException("Not valid lug number");
//		}
//		f11[lugNumber-1] = frequency;
//	}

	//returns estimated tuning rod turn angles to achieve tuning
	public double[] getAngles(double[] f11) {
//		if(!readyToTune()) {
//			throw new NullPointerException("Give all drum strike samples");
//		}
		if(desiredFreq==0) {
			throw new IllegalStateException("Enter desired frequency");
		}

		thetas = tuningOracle(f11);

		// perform check that we don't have angles that are too high here i.e likely to stretch head
		// if we perform tuning. Or perform in presenter

		return thetas;
	}

	private double[] tuningOracle(double[] f11) {
		double f11DesSq = desF11*desF11; // square frequency for linearity of best-fit model
		double f11Sq[] = new double[f11.length]; // square all frequency inputs for linearity
		for (int i = 0; i<f11.length; i++) {
			f11Sq[i] = f11[i]*f11[i];
		}


		for (int i = 0; i<f11.length; i++) {
			for (int j = 0; j<f11.length; j++) {
				thetas[i] += (f11DesSq-f11Sq[j])* tuningCoeffs8Lugs[j];
			}
			f11Sq = arrayRotate(f11Sq, 1);
		}

		return thetas;
	}

	private double[] arrayRotate(double array[], int shift) {
		int arrayLen = array.length;
		double shiftArray[] = new double[array.length];
		for (int i = 0; i<arrayLen; i++) {
			shiftArray[(i+shift)%array.length] = array[i];
		}
		return shiftArray;
	}

	//if all 8 drum strikes have been recorded, ready to analyze and tune
	public boolean readyToTune() {
		for(int i = 0;i<f11.length;i++) {
			if (f11[i] == 0) {
				return false;
			}
		}
		return true;
	}

	public void deleteSamples() {
		for(int i = 0;i<f11.length;i++) {
			f11[i] = 0;
		}
	}



}
