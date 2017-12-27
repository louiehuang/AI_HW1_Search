package HW1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

public class DFS {

	int n; // The width and height of the square nursery
	int p; // The number of baby lizards
	char[][] states; //
	String method; // "DFS", "BFS", "SA"
	String file_path; //input

	int solution_num = 0; //number of solutions
	HashSet<Integer> trees = new HashSet<>(); //Trees' positions, [0, n*n)
	
	public DFS() {
	}
	
	static class StopRecursionException extends RuntimeException {
		private static final long serialVersionUID = -417889252996694067L;
    }
	
	public boolean isNoSolution(){
		if(p > n && trees.size() == 0){
//			System.out.println("No Soltuon");
			return true;
		}
		return false;
	}
	
	
	/**
	 * Check n * n positions
	 * @param set_num The number of lizards that has been placed
	 * @param cur_queen The position which is trying currently, max is n*n-1
	 */
	public void runDFS(int cur_queen, int set_num) {

		if (set_num == p) { // All the lizards have been set, solution found
			solution_num++;
//			printState();
			
			writeResult();
			throw new StopRecursionException(); //Stop when 1 solution found, throw an exception to jump out all recursions
//			return; // Backtrack to find next solution
		} else {
			for (int i = cur_queen; i < n * n; i++) { //[0, cur_queen) has been processed
				int cur_row = i / n, cur_col = i % n; //coordinations
				
				if(states[cur_row][cur_col] == '2') //if current position is tree
					continue;
				
				if (isValid(i)) { //current position is valid
					states[cur_row][cur_col] = '1'; //try to place a lizard at this position
					addBarriers(i); //Add barriers to this position's same row/col/diag, speed up processing, set as 3

//					printState();
					
					runDFS(i + 1, set_num + 1); // next position & lizard
					
					removeBarriers(i); //restore all 3 to 0
					states[i / n][i % n] = '0'; //remove lizard
				}
				
			}
		}
	}
	
	

	/**
	 * Check position before max_queen(current lizard's position) to see if there exists a conflict
	 * If no conflict return true，otherwise return false
	 */
	public boolean isValid(int max_queen) {
		int cur_row = max_queen / n, cur_col = max_queen % n; //10 -> (1,2)
		// if current position is tree or barrier, which means this position is not '0', return false
		if (states[cur_row][cur_col] != '0'){
			return false;
		}
		
		for (int i = 0; i < max_queen; i++) { // i < cur_queen, lizards placed earlier
			// Two lizards in same row && not tree between these two lizards ||
			// Two lizards in same col && not tree between these two lizards ||
			// Two lizards in same diag && not tree between these two lizards
			int prev_row = i / n, prev_col = i % n;	
			
			//just judge lizards(1) to see whether there is attack, we do not care about tree or barrier or empty space(0)
			if(states[prev_row][prev_col] != '1')
				continue;
						
			if (prev_row == cur_row && !hasTree("row", cur_row, prev_col, cur_col)
				|| prev_col == cur_col && !hasTree("col", cur_col, prev_row, cur_row)
				|| Math.abs(prev_row - cur_row) == Math.abs(prev_col - cur_col)
							&& !hasTree(prev_row, prev_col, cur_row, cur_col)) {
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
		if (method.equals("col")) { //2,0,0
			for (int i = p1 + 1; i < p2; i++) {
				if (states[i][index] == '2') {
					return true;
				}
			}
		} else if (method.equals("row")) {
			for (int j = p1 + 1; j < p2; j++) {
				if (states[index][j] == '2') {
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
		if (j2 > j1) { // (i2,j2) is bottom right to (i1,j1)
			for (int i = i1 + 1, j = j1 + 1; i < i2 && j < j2; i++, j++) { //i++,j++, go bottom right
				if (states[i][j] == '2')
					return true;
			}
		} else { // (i2,j2) is bottom left to (i1,j1)
			for (int i = i1 + 1, j = j1 - 1; i < i2 && j > j2; i++, j--) { //i++,j--, go bottom left
				if (states[i][j] == '2')
					return true;	
			}
		}
		return false;
	}

	
	
	
	/**
	 * Print state
	 */
	public void printState() {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				System.out.print((states[i][j] == '3' ? '0' : states[i][j])  + ((j+1) % n == 0 ? "" : " "));
			}
			System.out.println();
		}
		System.out.println();
	}
	

	public DFS(String filePath) {
		this.file_path = filePath;
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
						method = lineContent;
					} else if (count == 1) {
						this.n = Integer.parseInt(lineContent);
					} else if (count == 2) {
						this.p = Integer.parseInt(lineContent);
					} else { // count >= 3
						if (states == null) {
							states = new char[n][n];
						}
						for (int i = 0; i < lineContent.length(); i++) {
							states[count - 3][i] = (lineContent.charAt(i));
							if(lineContent.charAt(i) == '2'){
								trees.add(index); //Tree
							}
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

//		System.out.println(n + ", " + p);
//		printState();
	
		writeResult(); //Write FAIL first in case run time exceeds
		
	}
	
	
	
	public void writeResult(){
		String content = "";
		
		try {
			if(solution_num == 0){
				content = "FAIL";
			}else{
				content = "OK\n";
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						content += (states[i][j] == '3' ? '0' : states[i][j]);
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
	
	

	/**
	 * Set index's left in its row, down in its col and main diag & minor diag as 3, stop when encounter 2(tree)
	 * @param index
	 */
	public void addBarriers(int index){
		int start_row = index / n, start_col = index % n;
		int next_row = start_row + 1, next_col = start_col + 1;
		
		while(next_col < n && states[start_row][next_col] != '2'){ //col
			if(states[start_row][next_col] == '0')
				states[start_row][next_col] = '3';
			next_col++;
		}
		
		while(next_row < n && states[next_row][start_col] != '2'){ //row
			if(states[next_row][start_col] == '0')
				states[next_row][start_col] = '3';
			next_row++;
		}

		//main diag from index
		for (int i = start_row + 1, j_right = start_col + 1; i < n && j_right < n; i++, j_right++) {
			if(states[i][j_right] == '0')
				states[i][j_right] = '3';
			if(states[i][j_right] == '2')
				break;
		}
		
		//minod diag from index
		for (int i = start_row + 1, j_left = start_col - 1; i < n && j_left >= 0; i++, j_left--) {
			if(states[i][j_left] == '0')
				states[i][j_left] = '3';
			if(states[i][j_left] == '2')
				break;
		}
		
	}
	
	/**
	 * Set index's left in its row, down in its col and main diag & minor diag as 0, stop when encounter 2(tree)
	 * @param index
	 */
	public void removeBarriers(int index){
		int start_row = index / n, start_col = index % n;
		for(int j = start_col + 1; j < n; j++){ //row
			if(states[start_row][j] == '3')
				states[start_row][j] = '0';
			if(states[start_row][j] == '2')
				break;
		}
		for(int i = start_row + 1; i < n; i++){ //column
			if(states[i][start_col] == '3')
				states[i][start_col] = '0';
			if(states[i][start_col] == '2')
				break;
		}
		
		
		
		/*
		 * No need to use hashmap to check whether barriers as pos is more than 1
		 * */
		//main diag from index
		for (int i = start_row + 1, j = start_col + 1; i < n && j < n; i++, j++) {
			if(states[i][j] == '3')
				states[i][j] = '0';
			if(states[i][j] == '2')
				break;
		}
		//minor diag from index
		for (int i = start_row + 1, j = start_col - 1; i < n && j >= 0; i++, j--) { //i++,j--, go bottom left
			if(states[i][j] == '3')
				states[i][j] = '0';
			if(states[i][j] == '2')
				break;	
		}	
	}

	
}
