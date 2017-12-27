package HW1;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


/**
 * online test platform seems does not support multiple threads, so use System.currentTimeMillis() to count time
 * @author hlyin
 *
 */
public class SA {

	int n; // The width and height of the square nursery
	int p; // The number of baby lizards

	List<Integer> lizards = new ArrayList<>(); //List<Lizard> are the positions of placed lizards (row, col)
	HashSet<Integer> trees = new HashSet<>(); //Trees' positions, [0, n*n)

	//just try one time, set maxItNums big, stop when reaching 5 mins
	long maxItNums = Integer.MAX_VALUE; // n * n
	
	int solution_num = 0;

	public SA() { }

	
	public int runSA() {
		
		//if (p > n && no trees) || (lizard is more than '0' space) then no solution
		if((p > n && trees.size() == 0) || (p > n*n - trees.size())) 
			return -2; 
			
		double temperature = n * n, ratio = 0.99;

		randomState(); //Generate random state	

		Collections.sort(lizards); //lizards has been initialized in randomState()
		int costToCompare = calcConflicts();
		
		if(costToCompare == 0){ //Solution found after generating random state
			solution_num++;
			writeResult(); //write to output.txt
			return 0;
		}
		
		long startTime_SA = System.currentTimeMillis();
		boolean timeout = false;

		for (int i = 0; i < maxItNums; i++) {
			//time out
			long currentTime_SA = System.currentTimeMillis();	
			//Note that maxItNums may be finished iteration before time out, in this case, -1 is returned
			if(currentTime_SA - startTime_SA >= 290000){ //5 * 60 * 1000 = 300 * 1000 > 290 * 1000 = 290000
				timeout = true;
				break;
			}
			
			nextStep(costToCompare, temperature);
			costToCompare = calcConflicts();
//			System.out.println("cost: " + costToCompare);
			
			if (costToCompare == 0) { // Succeed
				solution_num++;
				writeResult(); //write to output.txt
				return 0;
			}

			temperature = Math.max(temperature * ratio, 0.01);
//			System.out.println("temperature: " + temperature);
		}

		// Fail
//		System.out.println("No solution");
     
		if(timeout)
			return -2;
		
		return -1;
	}

	/**
	 * Choose next state
	 * @return
	 */
	public void nextStep(int costToCompare, double temperature) {	
//		HashSet<Integer> hasTried = new HashSet<>();
        while (true) {
        	int next_pos = (int) (Math.random() * (n * n));   	
    		
//        	if(hasTried.contains(next_pos))
//        		continue;
        	
        	if(lizards.contains(next_pos) || trees.contains(next_pos))
        		continue;
        	
        	//replace a lizard
        	int prev_lizard = (int) (Math.random() * (p)); //[0,p), index
        	int prev_pos = lizards.get(prev_lizard); //[0, n*n), value
        	lizards.set(prev_lizard, next_pos); //replace a lizard，index, value
      	
            int nextCost = calcConflicts();
//            System.out.println("costToCompare: " + costToCompare + ",  next cost: " + nextCost);
            
            int delta_E = nextCost - costToCompare; //Try to minimize E
            
            if (delta_E < 0) //next step is better, accept
            	break;
            else{
	            double accept_P = Math.min(1, Math.exp(-delta_E / temperature)); //accept probability
	            if (Math.random() < accept_P){ //accept
	                break;
	            }else{ //decline
		        	lizards.set(prev_lizard, prev_pos); //restore
//		        	hasTried.add(next_pos); //recode next_pos is not a good option, skip it next time generated
	            } 
            }
        } 
	}
	
	
	/**
	 * calculate the number of conflict, take trees into consideration
	 * @param node
	 * @return number of conflict
	 */
	public int calcConflicts() {
		
		int h = 0, size = lizards.size();	
		
		for(int i = 0; i < size - 1; i++){
			int cur_pos = lizards.get(i);
			int cur_row = cur_pos / n, cur_col = cur_pos % n;
//			System.out.println("cur: (" + cur_row + "," + cur_col + ")");
			
			for(int j = i+1; j < size; j++){
				int next_pos = lizards.get(j);
				int next_row = next_pos / n, next_col = next_pos % n;
//				System.out.println("next: (" + next_row + "," + next_col + ")");
				
				boolean sameRow = (cur_row == next_row);
				boolean sameCol = (cur_col == next_col);
				boolean sameDiag = (Math.abs(cur_row - next_row) == Math.abs(cur_col - next_col));
				
				if(!sameRow && !sameCol && !sameDiag){
					continue;
				}
				
				if(sameRow && !hasTree("row", cur_row, cur_col, next_col)
					|| sameCol && !hasTree("col", cur_col, cur_row, next_row)
					|| sameDiag && !hasTree(cur_row, cur_col, next_row, next_col)){
					h++;
				}
//				System.out.println(h);
			}		
		}
		return h;
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
			for (int i = p1 + 1; i < p2; i++) { //i row
				if (trees.contains(i * n + index)) {
					return true;
				}
			}
		} else if (method.equals("row")) {
			for (int j = p1 + 1; j < p2; j++) { //j col
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
	 * Generate initial state(lizards)
	 * @return
	 */
	public void randomState() {
		int count = 0;
		while(true) {
			if(count == p)
				break;		
			int pos = (int) (Math.random() * (n * n));
			if(lizards.contains(pos) || trees.contains(pos)){
				continue;
			}else{
				lizards.add(pos); //add lizard
				count++;
//				printState();
			}	
		}
//		System.out.println("Initial State");
//		printState();
	}
	
	
	
	/**
	 * Print state
	 */
	public void printState() {
		int[] state = new int[n * n];
		for(int lizard : lizards){
			state[lizard] = 1;
		}
		for(int tree : trees){
			state[tree] = 2;
		}
		
		for (int i = 0; i < n * n; i++) {
			System.out.print(state[i] + ((i+1) % n == 0 ? "\r\n" : " "));
		}
		System.out.println();
		
	}
	
	
	public SA(String filePath) {
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
							if(lineContent.charAt(i) == '2')
								trees.add(index);
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

		maxItNums = n * n < maxItNums ? maxItNums : n * n;
		
//		System.out.println(n + ", " + p);
//		printState();
		
		writeResult();
		
	}
	
	
	
	public void writeResult(){
		String content = "";
		
		try {
			if(solution_num == 0){
				content = "FAIL";
			}else{
				content = "OK\n";	
				
				int[] state = new int[n * n];
				for(int lizard : lizards){
					state[lizard] = 1;
				}
				for(int tree : trees){
					state[tree] = 2;
				}
				
				for (int i = 0; i < n * n; i++) {
					if(i == n*n-1)
						content += state[i];
					else
						content += (state[i] + ((i+1) % n == 0 ? "\n" : ""));
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
