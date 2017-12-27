package HW1;

import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class BFS {
	int n; // The width and height of the square nursery
	int p; // The number of baby lizards
	int solution_num = 0; //number of solutions
	boolean found_solution = false;
	String file_path; //input

	Queue<List<Lizard>> queue; //List<Lizard> are the positions of placed lizards (row, col)
	HashSet<Integer> trees = new HashSet<>(); //Trees' positions, [0, n*n)
	
	//Lizard's position
	class Lizard {
		int row; 
		int col;
		public Lizard() { }
		public Lizard(int row, int col) {
			this.row = row;
			this.col = col;
		}
	}

	public BFS() {
	}

	public void runBFS() {
		if(p > n && trees.size() == 0){
			System.out.println("No Soltuon");
			return;
		}
		
		while (!queue.isEmpty()) {
			List<Lizard> node = queue.poll(); // pop state

			int size = node.size(); //number of placed lizards
			//position of last placed lizard
			int last_row = (size == 0 ? 0 : node.get(node.size() - 1).row);
			int last_col = (size == 0 ? 0 : node.get(node.size() - 1).col);
			
			for(int i = last_row * n + last_col; i < n * n; i++){
				if(isValid(node, i)){
					Lizard newLizard = new Lizard(i / n, i % n);
					node.add(newLizard); //put lizard at i
					newLizard = null; //suggest GC
					
//					printState(node, trees);
					
					//Goal
					if (node.size() == p) {
//						printState(node, trees);
						
						solution_num++;
						writeResult(node);  //print solution to file
						return; // stop when a solution is found
					}
					
					List<Lizard> copy = new ArrayList<>(node);
					
//					addBarriers(copy, i); //Needs extra memory... give up this strategy
					
					queue.offer(copy); // put
					copy = null;
				
					node.remove(node.size() - 1);
				}
			}
			
		}
	}
	

	/**
	 * judge whether is valid to put lizard at state(note)'s index position
	 * @param index
	 * @return true if valid
	 */
	public boolean isValid(List<Lizard> node, int index) {
		if (trees.contains(index)) { // current place is a tree
			return false;
		}

		int cur_row = index / n, cur_col = index % n;	
		for(Lizard prev : node){
			int prev_row = prev.row, prev_col = prev.col;
			
			boolean sameRow = (prev_row == cur_row);
			boolean sameCol = (prev_col == cur_col);
			boolean sameDiag = (Math.abs(prev_row - cur_row) == Math.abs(prev_col - cur_col));
			
			if(!sameRow && !sameCol && !sameDiag){
				continue;
			}
			
			// Two lizards in same row && not tree between these two lizards ||
			// Two lizards in same col && not tree between these two lizards ||
			// Two lizards in same diag && not tree between these two lizards
			if(sameRow && !hasTree("row", cur_row, prev_col, cur_col)
				|| sameCol && !hasTree("col", cur_col, prev_row, cur_row)
				|| sameDiag && !hasTree(prev_row, prev_col, cur_row, cur_col)){
				return false;
			}
		}
		return true;
	}

	/**
	 * Judge whether there is a tree between p1 and p2 (if method is "row", the p1 & p2 are col, vice versa)
	 * @param method "row" to judge whether there is a tree between (index,p1) and (index, p2),
	 * or "col" to judge whether there is a tree between (p1,index) and (p2,index)
	 * @param index row index or col index
	 * @return
	 */
	public boolean hasTree(String method, int index, int p1, int p2) {
		if (method.equals("col")) { // 2,0,0
			for (int i = p1 + 1; i < p2; i++) { //i is row
				if (trees.contains(i * n + index)) {
					return true;
				}
			}
		} else if (method.equals("row")) {
			for (int j = p1 + 1; j < p2; j++) { //j is col
				if (trees.contains(index * n + j)) { //same row, (index,p1)-> (index,p1+1)->...(index,p2)
					return true;
				}
			}
		}
		return false;
	}

	/** (1,0), (6,5)
	 * Assume (i1,j1) and (i2,j2) are in diagonal, judge whether tree exists between (i1,j1) and (i2,j2), i1 <= i2
	 */
	public boolean hasTree(int i1, int j1, int i2, int j2) {
		if (j2 > j1) { // (i2,j2) is bottom right to (i1,j1), i++,j++, go bottom right
			for (int i = i1 + 1, j = j1 + 1; i < i2 && j < j2; i++, j++) { 
				if (trees.contains(i * n + j))
					return true;
			}
		} else { // (i2,j2) is bottom left to (i1,j1), i++,j--, go bottom left
			for (int i = i1 + 1, j = j1 - 1; i < i2 && j > j2; i++, j--) { 
				if (trees.contains(i * n + j))
					return true;
			}
		}
		return false;
	}

	
	
	
	
	
	
	
	
	/**
	 * Print state
	 */
	public void printState(List<Lizard> node, HashSet<Integer> trees) {
		int[][] status = new int[n][n];
		
		for(Lizard lizard : node){
			status[lizard.row][lizard.col] = 1;
		}
		
		for(Integer tree : trees){
			status[tree / n][tree % n] = 2;
		}
		
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				System.out.print(status[i][j] + ((j+1) % n == 0 ? "" : " "));
			}
			System.out.println();
		}
		System.out.println();
	}

	
	public BFS(String filePath) {
		this.file_path = filePath;
		queue = new LinkedList<>();
		List<Lizard> initNode = new ArrayList<>();

		int count = 0;
		try {
			File file = new File(filePath);
			if (file.isFile() && file.exists()) {
				InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), "GBK"); //
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				String lineContent = null;
				int index = 0;
				while ((lineContent = bufferedReader.readLine()) != null) {
					if (count == 0) {
//						method = lineContent;
					} else if (count == 1) {
						this.n = Integer.parseInt(lineContent);
					} else if (count == 2) {
						this.p = Integer.parseInt(lineContent);
					} else { // count >= 3
						for (int i = 0; i < lineContent.length(); i++) {
							if(lineContent.charAt(i) == '2'){
								trees.add(index); 
							}
							index++;
						}	
					}
					count++; // next line
				}
				inputStreamReader.close();
			} else {
				System.out.println("cannot find file");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		queue.add(initNode); //Initial State is an empty List，No lizard
		
//		System.out.println(n + ", " + p);
//		printState(queue.peek(), trees);
		
		writeResult(initNode); //write FAIL at first and overwrite when a solution is found
		
	}
	
	
	public void writeResult(List<Lizard> node){
		String content = "";
		
		try {
			if(solution_num == 0){
				content = "FAIL";
			}else{
				content = "OK\n";		
				int[][] status = new int[n][n];
				for(Lizard lizard : node){
					status[lizard.row][lizard.col] = 1;
				}			
				for(Integer tree : trees){
					status[tree / n][tree % n] = 2;
				}		
				for(int i = 0; i < n; i++){
					for(int j = 0; j < n; j++){
						content += (status[i][j] + "");
					}
					content += (i == n -1) ? "" : "\n";
				}
			}

            File file = new File("output.txt");
            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
}
