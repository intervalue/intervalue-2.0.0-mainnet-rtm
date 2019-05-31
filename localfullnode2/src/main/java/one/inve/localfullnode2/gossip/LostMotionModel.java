package one.inve.localfullnode2.gossip;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: a helper curve to convert last motion round into sleeping
 *               time,{@code exponent} is a key factor
 * @author: Francis.Deng
 * @date: May 31, 2019 12:12:54 AM
 * @version: V1.0
 */
public class LostMotionModel {
	private final double exponent;

	public LostMotionModel(double exponent) {
		super();
		this.exponent = exponent;
	}

	public double getYVar(int lostMotionRound) {
		return Math.pow(lostMotionRound / 4, exponent);
	}

	public static void main(String[] args) {
		LostMotionModel lostMotionModel = new LostMotionModel(0.3);

		for (int i = 0; i < 1000; i++)
			System.out.println(lostMotionModel.getYVar(i));
	}

}
