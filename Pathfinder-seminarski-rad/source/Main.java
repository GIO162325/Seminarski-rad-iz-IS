import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JComboBox;

public class Main {

	//FRAME
	JFrame frame;
	//GENERAL VARIABLES
	private int cells = 20;
	private int delay = 30;
	private double dense = .5;
	private double density = (cells*cells)*.5;
	private int startx = -1;
	private int starty = -1;
	private int finishx = -1;
	private int finishy = -1;
	private int tool = 0;
	private int checks = 0;
	private int length = 0;
	private int curAlg = 0;
	private int WIDTH = 850;
	private final int HEIGHT = 650;
	private final int MSIZE = 600;
	private int CSIZE = MSIZE/cells;
	private String[] algorithms = {"BFS","A*","Best First","Hill climb","DFS"};
	private String[] tools = {"Start","Finish","Wall", "Eraser"};
	private boolean solving = false;
	Node[][] map;
	Algorithm Alg = new Algorithm();
	JSlider size = new JSlider(1,5,2);
	JSlider obstacles = new JSlider(1,100,50);
	JLabel algL = new JLabel("Algorithms");
	JLabel toolL = new JLabel("Toolbox");
	JLabel sizeL = new JLabel("Size:");
	JLabel cellsL = new JLabel(cells+"x"+cells);
	JLabel checkL = new JLabel("Checks: "+checks);
	JLabel lengthL = new JLabel("Path Length: "+length);
	JButton searchB = new JButton("Start Search");
	JButton resetB = new JButton("Reset");
	JButton clearMapB = new JButton("Clear Map");
	JComboBox algorithmsBx = new JComboBox(algorithms);
	JComboBox toolBx = new JComboBox(tools);
	JPanel toolP = new JPanel();
	Map canvas;
	Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

	public static void main(String[] args) {	//MAIN METHOD
		new Main();
	}

	public Main() {
		clearMap();
		initialize();
	}
	
	public void clearMap() {
		finishx = -1;	
		finishy = -1;
		startx = -1;
		starty = -1;
		map = new Node[cells][cells];
		for(int x = 0; x < cells; x++) {
			for(int y = 0; y < cells; y++) {
				map[x][y] = new Node(3,x,y);
			}
		}
		reset();	
	}
	
	public void resetMap() {
		for(int x = 0; x < cells; x++) {
			for(int y = 0; y < cells; y++) {
				Node current = map[x][y];
				if(current.getType() == 4 || current.getType() == 5)
					map[x][y] = new Node(3,x,y);
			}
		}
		if(startx > -1 && starty > -1) {
			map[startx][starty] = new Node(0,startx,starty);
			map[startx][starty].setHops(0);
		}
		if(finishx > -1 && finishy > -1)
			map[finishx][finishy] = new Node(1,finishx,finishy);
		reset();	//RESET SOME VARIABLES
	}

	private void initialize() {	//INITIALIZE THE GUI ELEMENTS
		frame = new JFrame();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setSize(WIDTH,HEIGHT);
		frame.setTitle("Pathfinder");
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		toolP.setBorder(BorderFactory.createTitledBorder(loweredetched,"Controls"));
		int space = 25;
		int buff = 45;
		
		toolP.setLayout(null);
		toolP.setBounds(10,10,210,600);
		
		searchB.setBounds(40,space, 120, 25);
		toolP.add(searchB);
		space+=buff;
		
		resetB.setBounds(40,space,120,25);
		toolP.add(resetB);
		space+=buff;
		
		clearMapB.setBounds(40,space, 120, 25);
		toolP.add(clearMapB);
		space+=40;
		
		algL.setBounds(40,space,120,25);
		toolP.add(algL);
		space+=25;
		
		algorithmsBx.setBounds(40,space, 120, 25);
		toolP.add(algorithmsBx);
		space+=40;
		
		toolL.setBounds(40,space,120,25);
		toolP.add(toolL);
		space+=25;
		
		toolBx.setBounds(40,space,120,25);
		toolP.add(toolBx);
		space+=buff;
		
		sizeL.setBounds(15,space,40,25);
		toolP.add(sizeL);
		size.setMajorTickSpacing(10);
		size.setBounds(50,space,100,25);
		toolP.add(size);
		cellsL.setBounds(160,space,40,25);
		toolP.add(cellsL);
		space+=buff;

		checkL.setBounds(15,space,100,25);
		toolP.add(checkL);
		space+=buff;
		
		lengthL.setBounds(15,space,100,25);
		toolP.add(lengthL);
		space+=buff;
		
		frame.getContentPane().add(toolP);
		
		canvas = new Map();
		canvas.setBounds(230, 10, MSIZE+1, MSIZE+1);
		frame.getContentPane().add(canvas);
		
		searchB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
				if((startx > -1 && starty > -1) && (finishx > -1 && finishy > -1))
					solving = true;
			}
		});
		resetB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetMap();
				Update();
			}
		});
		clearMapB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearMap();
				Update();
			}
		});
		algorithmsBx.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				curAlg = algorithmsBx.getSelectedIndex();
				Update();
			}
		});
		toolBx.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				tool = toolBx.getSelectedIndex();
			}
		});
		size.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				cells = size.getValue()*10;
				clearMap();
				reset();
				Update();
			}
		});
		obstacles.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				dense = (double)5/100;
				Update();
			}
		});
		startSearch();
	}
	
	public void startSearch() {
		if(solving) {
			switch(curAlg) {
				case 0:
					Alg.BFS();
					break;
				case 1:
					Alg.AStar();
					break;
				case 2:
					Alg.BestFirst();
					break;
				case 3:
					Alg.HillClimb();
					break;
				case 4:
					Alg.DFS();
					break;
			}
		}
		pause();
	}
	
	public void pause() {
		int i = 0;
		while(!solving) {
			i++;
			if(i > 500)
				i = 0;
			try {
				Thread.sleep(1);
			} catch(Exception e) {}
		}
		startSearch();
	}
	
	public void Update() {
		density = (cells*cells)*dense;
		CSIZE = MSIZE/cells;
		canvas.repaint();
		cellsL.setText(cells+"x"+cells);
		lengthL.setText("Path Length: "+length);
		checkL.setText("Checks: "+checks);
	}
	
	public void reset() {
		solving = false;
		length = 0;
		checks = 0;
	}

	public void delay() {
		try {
			Thread.sleep(delay);
		} catch(Exception e) {}
	}
	
	class Map extends JPanel implements MouseListener, MouseMotionListener{
		
		public Map() {
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			for(int x = 0; x < cells; x++) {
				for(int y = 0; y < cells; y++) {
					switch(map[x][y].getType()) {
						case 0:
							g.setColor(Color.GREEN);
							break;
						case 1:
							g.setColor(Color.RED);
							break;
						case 2:
							g.setColor(Color.BLACK);
							break;
						case 3:
							g.setColor(Color.WHITE);
							break;
						case 4:
							g.setColor(Color.CYAN);
							break;
						case 5:
							g.setColor(Color.YELLOW);
							break;
					}
					g.fillRect(x*CSIZE,y*CSIZE,CSIZE,CSIZE);
					g.setColor(Color.BLACK);
					g.drawRect(x*CSIZE,y*CSIZE,CSIZE,CSIZE);
				}
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			try {
				int x = e.getX()/CSIZE;	
				int y = e.getY()/CSIZE;
				Node current = map[x][y];
				if((tool == 2 || tool == 3) && (current.getType() != 0 && current.getType() != 1))
					current.setType(tool);
				Update();
			} catch(Exception z) {}
		}

		@Override
		public void mouseMoved(MouseEvent e) {}

		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			resetMap();
			try {
				int x = e.getX()/CSIZE;
				int y = e.getY()/CSIZE;
				Node current = map[x][y];
				switch(tool) {
					case 0: {
						if(current.getType()!=2) {
							if(startx > -1 && starty > -1) {
								map[startx][starty].setType(3);
								map[startx][starty].setHops(-1);
							}
							current.setHops(0);
							startx = x;
							starty = y;
							current.setType(0);
						}
						break;
					}
					case 1: {
						if(current.getType()!=2) {
							if(finishx > -1 && finishy > -1)
								map[finishx][finishy].setType(3);
							finishx = x;
							finishy = y;
							current.setType(1);
						}
						break;
					}
					default:
						if(current.getType() != 0 && current.getType() != 1)
							current.setType(tool);
						break;
				}
				Update();
			} catch(Exception z) {}
		}

		@Override
		public void mouseReleased(MouseEvent e) {}
	}

	class Algorithm {
		public void AStar() {
			ArrayList<Node> priority = new ArrayList<Node>();
			priority.add(map[startx][starty]);
			while(solving) {
				if(priority.size() <= 0) {
					solving = false;
					break;
				}
				int hops = priority.get(0).getHops()+1;
				ArrayList<Node> explored = exploreNeighbors(priority.get(0),hops);
				if(explored.size() > 0) {
					priority.remove(0);
					priority.addAll(explored);
					Update();
					delay();
				} else {
					priority.remove(0);
				}
				sortQue(priority);
			}
		}

		public void BestFirst() {
			ArrayList<Node> priority = new ArrayList<>();
			priority.add(map[startx][starty]);
			while (solving) {
				if (priority.size() <= 0) {
					solving = false;
					break;
				}
				Node current = priority.get(0);
				int hops = current.getHops() + 1;
				ArrayList<Node> explored = exploreNeighbors(current, hops);
				if (explored.size() > 0) {
					priority.remove(0);
					priority.addAll(explored);
					Update();
					delay();
				} else {
					priority.remove(0);
				}
				sortQue(priority);
			}
		}



		public void DFS() {
			Stack<Node> stack = new Stack<>();
			stack.push(map[startx][starty]);

			while (solving) {
				if (stack.isEmpty()) {
					solving = false;
					break;
				}
				Node current = stack.pop();
				int hops = current.getHops() + 1;

				ArrayList<Node> explored = exploreNeighbors(current, hops);
				for (Node neighbor : explored) {
					stack.push(neighbor);
				}

				Update();
				delay();
			}
		}

		public void BFS() {
			Queue<Node> queue = new LinkedList<>();
			queue.add(map[startx][starty]);

			while (solving) {
				if (queue.isEmpty()) {
					solving = false;
					break;
				}
				Node current = queue.poll();
				int hops = current.getHops() + 1;

				ArrayList<Node> explored = exploreNeighbors(current, hops);
				queue.addAll(explored);

				Update();
				delay();
			}
		}

		public void HillClimb() {
			ArrayList<Node> priority = new ArrayList<>();
			priority.add(map[startx][starty]);

			while (solving) {
				if (priority.isEmpty()) {
					solving = false;
					break;
				}
				Node current = priority.remove(0);
				int hops = current.getHops() + 1;

				ArrayList<Node> explored = exploreNeighbors(current, hops);
				if (!explored.isEmpty()) {
					// sortiranje suseda prema heuristici (manje je bolje)
					explored.sort(Comparator.comparingDouble(Node::getEuclidDist));
					priority.add(explored.get(0));
				}

				Update();
				delay();
			}
		}


		public ArrayList<Node> sortQue(ArrayList<Node> sort) { //za algoritme sa heuristikom
			int c = 0;
			while(c < sort.size()) {
				int sm = c;
				for(int i = c+1; i < sort.size(); i++) {
					if(sort.get(i).getEuclidDist()+sort.get(i).getHops() < sort.get(sm).getEuclidDist()+sort.get(sm).getHops())
						sm = i;
				}
				if(c != sm) {
					Node temp = sort.get(c);
					sort.set(c, sort.get(sm));
					sort.set(sm, temp);
				}	
				c++;
			}
			return sort;
		}

		
		public ArrayList<Node> exploreNeighbors(Node current, int hops) {
			ArrayList<Node> explored = new ArrayList<Node>();
			for(int a = -1; a <= 1; a++) {
				for(int b = -1; b <= 1; b++) {
					int xbound = current.getX()+a;
					int ybound = current.getY()+b;
					if((xbound > -1 && xbound < cells) && (ybound > -1 && ybound < cells)) {
						Node neighbor = map[xbound][ybound];
						if((neighbor.getHops()==-1 || neighbor.getHops() > hops) && neighbor.getType()!=2) {
							explore(neighbor, current.getX(), current.getY(), hops);
							explored.add(neighbor);
						}
					}
				}
			}
			return explored;
		}
		
		public void explore(Node current, int lastx, int lasty, int hops) {
			if(current.getType()!=0 && current.getType() != 1)
				current.setType(4);
			current.setLastNode(lastx, lasty);
			current.setHops(hops);
			checks++;
			if(current.getType() == 1) {
				backtrack(current.getLastX(), current.getLastY(),hops);
			}
		}
		
		public void backtrack(int lx, int ly, int hops) {
			length = hops;
			while(hops > 1) {
				Node current = map[lx][ly];
				current.setType(5);
				lx = current.getLastX();
				ly = current.getLastY();
				hops--;
			}
			solving = false;
		}
	}
	
	class Node {

		private int cellType = 0;
		private int hops;
		private int x;
		private int y;
		private int lastX;
		private int lastY;
		private double dToEnd = 0;
	
		public Node(int type, int x, int y) {
			cellType = type;
			this.x = x;
			this.y = y;
			hops = -1;
		}
		
		public double getEuclidDist() {		//Rastojanje od Start kvadrata do Finish kvadrata koriscenjem formule za rastojanje
			                                 //izmedju 2 tacke
			int xdif = Math.abs(x-finishx);
			int ydif = Math.abs(y-finishy);
			dToEnd = Math.sqrt((xdif*xdif)+(ydif*ydif));
			return dToEnd;
		}
		
		public int getX() {return x;}
		public int getY() {return y;}
		public int getLastX() {return lastX;}
		public int getLastY() {return lastY;}
		public int getType() {return cellType;}
		public int getHops() {return hops;}
		
		public void setType(int type) {cellType = type;}
		public void setLastNode(int x, int y) {lastX = x; lastY = y;}
		public void setHops(int hops) {this.hops = hops;}

	}
}
