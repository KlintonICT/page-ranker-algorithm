//Name(s): Dujnapa Tanundet, Klinton Chhun, Arada Puengmongkolchaikit
//ID: 6088105, 6088111, 6088133
//Section: 1
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class implements PageRank algorithm on simple graph structure.
 * Put your name(s), ID(s), and section here.
 *
 */
public class PageRanker {
	private Set<String> data = new LinkedHashSet<String>();
	private Map<Integer, Double> pageRank= new HashMap<Integer, Double>();
	private Map<Integer, Set<Integer>> M = new HashMap<Integer, Set<Integer>>(); // M(p) is the set of pages that link to page p 
	private Map<Integer, Set<Integer>> L = new HashMap<Integer, Set<Integer>>(); // L(q) is the number of out_links form page q
	private Set<Integer> S = new LinkedHashSet<Integer>(); // the set of sink nodes, i.e pages that have no out links
	private Set<Integer> P = new LinkedHashSet<Integer>(); // the set of all pages
	private double d = 0.85;
	private double oldIteration = -1.0;
	private double newIteration = 0.0;
	private int countIteration = 1;
	
	/**
	 * This class reads the direct graph stored in the file "inputLinkFilename" into memory.
	 * Each line in the input file should have the following format:
	 * <pid_1> <pid_2> <pid_3> .. <pid_n>
	 * 
	 * Where pid_1, pid_2, ..., pid_n are the page IDs of the page having links to page pid_1. 
	 * You can assume that a page ID is an integer.
	 */
	public void loadData(String inputLinkFilename){
		try {
			File file = new File(inputLinkFilename);
			Scanner line = new Scanner(file);
			while(line.hasNextLine()) {
				data.add(line.nextLine());
			}
		}catch(Exception error) {
			System.out.println("Cannot load data.");
			error.printStackTrace();
		}
	}
	
	/**
	 * This method will be called after the graph is loaded into the memory.
	 * This method initialize the parameters for the PageRank algorithm including
	 * setting an initial weight to each page.
	 */
	public void initialize(){
		for(String dat: data) {
			String[] token = dat.split(" ");
			Integer[] tokens = new Integer[token.length];
			for(int i = 0; i < token.length; i++) tokens[i] = Integer.parseInt(token[i]);
			P.addAll(new LinkedHashSet<Integer>(Arrays.asList(Arrays.copyOfRange(tokens, 0, tokens.length))));
			for(int i = 1; i < tokens.length; i++) {
				if(!L.containsKey(tokens[i])) {
					L.put(tokens[i], new LinkedHashSet<Integer>(Arrays.asList(tokens[0])));
				}
				else L.get(tokens[i]).add(tokens[0]);
			}
			M.put(tokens[0], new LinkedHashSet<Integer>(Arrays.asList(Arrays.copyOfRange(tokens, 1, tokens.length))));
		}
		for(Integer page: P) {
			if(!L.containsKey(page)) S.add(page);
			pageRank.put(page, 1.0 / P.size());
		}
	}
	
	/**
	 * Computes the perplexity of the current state of the graph. The definition
	 * of perplexity is given in the project specs.
	 */
	public double getPerplexity(){
		double perplexity = 0.0;
		for(Integer page: P) {
			double rank = pageRank.get(page);
			perplexity += rank * Math.log(rank) / Math.log(2);
		}
		return Math.pow(2, -perplexity);
	}
	
	/**
	 * Returns true if the perplexity converges (hence, terminate the PageRank algorithm).
	 * Returns false otherwise (and PageRank algorithm continue to update the page scores). 
	 */
	public boolean isConverge(){
		if(((int)oldIteration % 10) == ((int)newIteration%10)) {
			countIteration += 1;
			if(countIteration < 4) return false;
			else return true;
		}
		else {
			oldIteration = newIteration;
			countIteration = 1;
		}
		return false;
	}
	
	/**
	 * The main method of PageRank algorithm. 
	 * Can assume that initialize() has been called before this method is invoked.
	 * While the algorithm is being run, this method should keep track of the perplexity
	 * after each iteration. 
	 * 
	 * Once the algorithm terminates, the method generates two output files.
	 * [1]	"perplexityOutFilename" lists the perplexity after each iteration on each line. 
	 * 		The output should look something like:
	 *  	
	 *  	183811
	 *  	79669.9
	 *  	86267.7
	 *  	72260.4
	 *  	75132.4
	 *  
	 *  Where, for example,the 183811 is the perplexity after the first iteration.
	 *
	 * [2] "prOutFilename" prints out the score for each page after the algorithm terminate.
	 * 		The output should look something like:
	 * 		
	 * 		1	0.1235
	 * 		2	0.3542
	 * 		3 	0.236
	 * 		
	 * Where, for example, 0.1235 is the PageRank score of page 1.
	 * 
	 */
	public void runPageRank(String perplexityOutFilename, String prOutFilename){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(perplexityOutFilename));
			BufferedWriter writer1 = new BufferedWriter(new FileWriter(prOutFilename));
			Map<Integer, Double> newPR = new HashMap<Integer, Double>();
			double sizeP = P.size();
			while(!isConverge()) {
				double sinkPR = 0.0;
				for(Integer p: S) sinkPR += pageRank.get(p); 
				for(Integer p: P) {
					double tempNewPR = (1 - d) / sizeP;
					tempNewPR += d * sinkPR / sizeP;
					if(M.containsKey(p)) {
						for(Integer q: M.get(p)) tempNewPR += d * pageRank.get(q) / L.get(q).size();
					}
					newPR.put(p, tempNewPR);
				}
				pageRank.putAll(newPR);
				newIteration = getPerplexity();
				writer.append(newIteration + "\n");
			}
			for(Map.Entry<Integer, Double> page : pageRank.entrySet()) writer1.append(page.getKey() + " " + page.getValue() + "\n");
			writer.close();
			writer1.close();
		}catch(Exception error) {
			System.out.println("Cannot write perplexity and PR score");
			error.printStackTrace();
		}
	}
	
	
	/**
	 * Return the top K page IDs, whose scores are highest.
	 */
	public Integer[] getRankedPages(int K){
		Integer[] rankedPages = new Integer[K];
		int i = 0;
		Map<Integer, Double> pageRankSort = pageRank.entrySet().stream()
												   .sorted((Map.Entry.comparingByValue(Comparator.reverseOrder())))
												   .limit(K)
												   .collect(Collectors.toMap(
														   Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		for(Map.Entry<Integer, Double> pageId: pageRankSort.entrySet()) {
			rankedPages[i++] = pageId.getKey();
		}
		return rankedPages;
	}
	
	public static void main(String args[]){
		long startTime = System.currentTimeMillis();
		PageRanker pageRanker =  new PageRanker();
		pageRanker.loadData("citeseer.dat");
		pageRanker.initialize();
		pageRanker.runPageRank("perplexity.out", "pr_scores.out");
		Integer[] rankedPages = pageRanker.getRankedPages(100);
		double estimatedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
		
		System.out.println("Top 100 Pages are:\n"+Arrays.toString(rankedPages));
		System.out.println("Proccessing time: "+estimatedTime+" seconds");
	}
}
