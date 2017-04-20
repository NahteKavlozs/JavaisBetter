package main.engine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

import main.entities.Bad;
import main.entities.Player;
import main.entities.Stage1;
import main.entities.Talis;
import main.entities.eTalis;

@SuppressWarnings("serial")
public class GamePanel extends JPanel implements Runnable, KeyListener{
	public static int Width = 1200;
	public static int Height = 800;

	private Thread thread;

	private boolean running;

	private BufferedImage image;
	private Graphics2D g;

	public static double score;
	public static double graze;
	private int FPS;
	private double averageFPS;

	@SuppressWarnings("unused")
	private Stage1 meme;
	public static void point(){
		score++;
	}

	public static void setScore(int scoreplus){
		score += scoreplus;
	}
	public enum GameState{
		PLAY, END, MENU
	}

	public static GameState getState(){
		return state;
	}

	private static GameState state;

	public String status = "Keep it Up";

	public static Player lilly;
	public static ArrayList<Talis> shots;
	public static ArrayList<eTalis> eShot;
	public static ArrayList<Bad> eList;

	private static int Pointer;

	private static String[] stageListing;

	public GamePanel(){
		super();
		FPS = 60;
		setPreferredSize(new Dimension(Width, Height));
		setFocusable(true);
		requestFocus();
		score = 0;

		stageListing = new String[3];

		for(int i = 0; i < stageListing.length; i++){
			stageListing[i] = ("Stage " + Integer.toString(i + 1));
		}
		Pointer = 0;
		state = GameState.MENU;
		lilly = new Player();
		shots = new ArrayList<Talis>();
		eShot = new ArrayList<eTalis>();
		eList = new ArrayList<Bad>();

	}

	@Override
	public void keyPressed(KeyEvent Key) {
		int keyCode = Key.getKeyCode();
		switch(state){
		case MENU:
			if(keyCode == KeyEvent.VK_LEFT){
				if((Pointer - 1) >= 0){
					Pointer--;
				}
			}
			if(keyCode == KeyEvent.VK_RIGHT){
				if((Pointer+1) < stageListing.length){
					Pointer++;
				}
			}
			if(keyCode == KeyEvent.VK_Z){
				try{
					switch(Pointer){
					case 0:
						new Stage1();
						state = GameState.PLAY;
						break;
					case 1:
						break;
					case 2:
						break;
					default:
						break;
					}
				}catch(ArrayIndexOutOfBoundsException e){
					System.out.println("LEL");
				}
			}
			break;
		case PLAY:
			if(keyCode == KeyEvent.VK_LEFT){
				lilly.setLeft(true);
			}
			if(keyCode == KeyEvent.VK_RIGHT){
				lilly.setRight(true);
			}
			if(keyCode == KeyEvent.VK_UP){
				lilly.setUp(true);
			}
			if(keyCode == KeyEvent.VK_DOWN){
				lilly.setDown(true);
			}
			if(keyCode == KeyEvent.VK_SHIFT){
				lilly.setFocus(true);
				FPS = 1;
			}
			if(keyCode == KeyEvent.VK_Z){
				lilly.setFiring(true);
			}
			break;
		case END:
			break;
		default:
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent Key) {
		int keyCode = Key.getKeyCode();
		if(keyCode == KeyEvent.VK_LEFT){
			lilly.setLeft(false);
		}
		if(keyCode == KeyEvent.VK_RIGHT){
			lilly.setRight(false);
		}
		if(keyCode == KeyEvent.VK_UP){
			lilly.setUp(false);
		}
		if(keyCode == KeyEvent.VK_DOWN){
			lilly.setDown(false);
		}
		if(keyCode == KeyEvent.VK_SHIFT){
			lilly.setFocus(false);
			FPS = 60;
		}
		if(keyCode == KeyEvent.VK_Z){
			lilly.setFiring(false);
		}

	}

	@Override
	public void keyTyped(KeyEvent Key){
	}

	//Functions
	public void addNotify(){
		super.addNotify();
		if(thread == null){
			thread = new Thread(this);
			thread.start();
		}

		addKeyListener(this);
	}

	@SuppressWarnings("static-access")
	@Override
	public void run() {

		running = true;

		image  = new BufferedImage(Width, Height, BufferedImage.TYPE_INT_RGB);

		g = (Graphics2D) image.getGraphics();

		long startTime;
		long URDTimeMillis;
		long waitTime;
		long totalTime = 0;

		int frameCount = 0;
		int maxFrameCount = 60;

		

		long targetTime = 1000 / FPS;



		while(running){
			startTime = System.nanoTime();
			gameUpdate();
			gameRender();
			gameDraw();			
			URDTimeMillis = (System.nanoTime() - startTime)/1000000;

			waitTime = targetTime - URDTimeMillis;

			try{
				thread.sleep(waitTime);
			}catch(Exception e){
				thread.interrupt();
			}

			totalTime += System.nanoTime() - startTime;
			frameCount++;

			if(frameCount == maxFrameCount){
				averageFPS = 1000.0 / ((totalTime / frameCount) / 1000000);
				frameCount = 0;
				totalTime = 0;
			}

		}
	}
	public int i = 0;
	private void gameUpdate(){
		switch(state){
		case MENU:
			break;
		case PLAY:
			lilly.update();
			//You
			for(int i = 0; i < eList.size(); i++){
				eList.get(i).update(lilly);
				if(eList.get(i).isDead()){
					eList.remove(i);
					i--;
				}

			}

			//Your shots
			for(int i = 0; i < shots.size(); i++){
				boolean remove = shots.get(i).update();
				if(remove){
					shots.remove(i);
					i--;

				}
			}

			//Enemy Shots
			for(int j = 0; j < eShot.size(); j++){
				boolean remove = eShot.get(j).update();
				if(remove){
					eShot.remove(j);
					j--;

				}
			}

			//Hit Detection
			for(int i = 0; i < shots.size(); i++){
				Talis b = shots.get(i);
				double bx = b.getX();
				double by = b.getY();
				double br = b.getR();
				for(int j = 0; j < eList.size(); j++){
					double ex = eList.get(j).getX();
					double ey = eList.get(j).getY();
					double er = eList.get(j).getR();

					double dx = bx - ex;
					double dy = by - ey;
					double dist = Math.sqrt(dx*dx + dy*dy);

					if(dist < br + er){
						eList.get(j).hit(5);
						shots.remove(i);
						i--;
						break;
					}
				}
			}

			//Enemy Bullet Hit Detection
			for(int k = 0; k < eShot.size(); k++){

				eTalis c = 	eShot.get(k);
				double px = c.getX();
				double py = c.getY();
				double projRadius = c.getR();

				double ex = lilly.getX();
				double ey = lilly.getY();
				double playerRadius = lilly.getR();

				double dx = px - ex;
				double dy = py - ey;
				double dist = Math.sqrt(dx*dx + dy*dy);
				if(projRadius > 2){
					if(dist < projRadius + playerRadius&&dist >= 2){
						graze++;
					}			
					if(dist < projRadius/2){
						score -= 1500;
						eShot.remove(k);
						takeLife();
						k--;
						break;
					}
				}
				else{
					if(dist < projRadius + playerRadius&&dist >= 2){
						graze++;
					}			
					if(dist < projRadius){
						score -= 1500;
						eShot.remove(k);
						takeLife();
						k--;
						break;
					}
				}

				//Clears screen after enemies die
				if(eList.size() == 0){
					for(int i = 0; i< eShot.size(); i++){
						eShot.remove(i);
						score += (1 + (0.001 * graze));
						i--;
					}
				}
			}
			break;
		case END:
			break;
		default:
			break;
		}
	}

	private void takeLife(){

		if(lilly.getLives() > 0){
			lilly.setLives(lilly.getLives() - 1);
		}
		switch(lilly.getLives()){
		case 0 :
			GamePanel.state = GameState.END;
			status = "dead xd";
			break;

		case 1 :
			status = "oh no";
			break;

		case 2 :
			status = "almost dead xd";
			break;

		default:
			break;

		}

	}
	private void gameRender(){

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 800, 800);
		g.setColor(Color.BLACK);

		g.fillRect(800, 0, 1200, 800);

		switch(state){
		case MENU:
			g.drawString("Play", 50, 100);
			g.drawString("Exit", 50, 120);

			for(int i = 0; i < stageListing.length; i++){
				if(i == Pointer&&!stageListing[i].contains(">")){
					stageListing[i] = ">" + stageListing[i];
				}
				if(i != Pointer&&stageListing[i].contains(">")){
					stageListing[i] = stageListing[i].replaceFirst(">", "");
				}
			}
			for(int i = 0; i < stageListing.length; i++){
				g.drawString(stageListing[i], 200 + (150*i), 400);
			}

			break;
		case PLAY:
			for(int i = 0; i < shots.size(); i++){
				shots.get(i).draw(g);
			}
			for(int j = 0; j < eShot.size(); j++){
				eShot.get(j).draw(g);
			}
			for(int i = 0; i < eList.size(); i++){
				eList.get(i).draw(g);
			}
			lilly.draw(g);
			break;
		case END:
			g.drawString("Your score was:" + Double.toString((int)score), 150, 200);
			String howgood = null;
			if(score < 0){howgood = "F";};
			if(score >= 0&&score < 499){howgood = "D";};
			if(score >= 500&&score < 1000){howgood = "C";};
			if(score >= 1001&&score < 2499){howgood = "B";};
			if(score >= 2500&&score < 4999){howgood = "A";};
			if(score >= 5000&&score < 7499){howgood = "S";};
			if(score >= 7500&&score < 9999){howgood = "SS";};
			if(score >= 10000){howgood = "SSS";};
			g.drawString(howgood, 150, 220);
			break;
		default:
			break;
		}
		g.setColor(Color.BLACK);
		g.fillRect(800, 0, 400, 800);
		g.setColor(Color.WHITE);
		g.drawString("Score: " + Integer.toString((int)score), 900, 40);
		g.drawString("Graze:" + Integer.toString((int)graze), 900, 90);
		g.drawString("Lives: " + Integer.toString(lilly.getLives()), 900, 120);
		g.drawString("FPS: " + Double.toString(averageFPS), 900, 150);
		g.drawString(status , 900, 140);

	}

	private void gameDraw(){
		Graphics g2  = this.getGraphics();
		g2.drawImage(image, 0, 0, null);
	}

}
