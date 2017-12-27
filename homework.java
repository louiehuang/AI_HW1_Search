package HW1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class homework {

	public static void main(String[] args) {		
		long start = System.currentTimeMillis();
		
		String inputFilePath = "input.txt";
		
		//get input file path from args
		if(args.length != 0)
			inputFilePath = args[0];
		
		//Judge which method should be selected
		String method = chooseMethod(inputFilePath).toUpperCase();
				
		if(method.equals("DFS")){
			DFS dfs = new DFS(inputFilePath);
			if(!dfs.isNoSolution()){
				try{
					dfs.runDFS(0, 0);
				}catch(Exception e){
				}
			}
//			System.out.println(dfs.solution_num);
		}else if(method.equals("BFS")){
			BFS bfs = new BFS(inputFilePath);
			bfs.runBFS();
//			System.out.println(bfs.solution_num);
		}else{
			//SA
			int found = -1;
//			int cur = 0, maxTry = 10; //just try once
			while(found < 0){ //found < 0 && cur < maxTry
				SA sa = new SA(inputFilePath);
				found = sa.runSA(); //return 1 if a solution is found
//				cur++;
				System.out.println(found);
				if(found == -2)
					break;
			}
		}
		
		long end = System.currentTimeMillis();
		System.out.println((end-start) + "ms");
		
	}
	
	
	public static String chooseMethod(String filePath){
		String method ="DFS"; //DFS, BFS, SA
		try {
			File file = new File(filePath);
			if (file.isFile() && file.exists()) {
				InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), "GBK"); //
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				method = bufferedReader.readLine(); //line 0
				inputStreamReader.close();
			} else {
				System.out.println("cannot find file in homework.java");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return method;
	}
	
}
