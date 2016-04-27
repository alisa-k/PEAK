###################################################################################################
# PEAK 
###################################################################################################
---------------------------------------------------------------------------------------------------
About
---------------------------------------------------------------------------------------------------
This package provides a Java implementation of PEAK, the first tool to 
automatically generate the pyramid models that enables the whole automatical 
assessment of both human and machine summaries.

Tutorial: http://www.larayang.com/peak/
Paper: http://www.larayang.com/peak/others/peak.pdf
---------------------------------------------------------------------------------------------------
Dependencies 
---------------------------------------------------------------------------------------------------
1. ADW: https://github.com/pilehvar/ADW
2. ClausIE: http://resources.mpi-inf.mpg.de/d5/clausie/clausie-0-0-1.zip
---------------------------------------------------------------------------------------------------
Source Code Files
---------------------------------------------------------------------------------------------------
***************************************************************************************************
Input
***************************************************************************************************

Each model summary and target summary input is processed with ClausIE. 
The config file, config.txt, contains two lines. The first line is the directory of the model
summaries, and the second is the directory of the target summaries. 

* ConfigFileReader.java
	- Reads the config file, contains methods that return lists of target or model summaries

***************************************************************************************************
Building The Pyramid: step1.java - step11.java 
***************************************************************************************************

* step1.java: 
	- Creates the hypergraph, g. 
	- Outputs step1-out.txt

* step2.java: 
	- Rearranges output from step1-out.txt such that score is first.
	- Outputs step2-out.txt, step2-out-SimilarityClass.txt

* step3.java: 
	- Finds the salient nodes in the triples (uses step2-out.txt)
	- Outputs salient nodes to step3-out.txt

* step4.java: 
	- Sorts step2-out-SimilarityClass.txt output in reverse order.
	- Outputs step2-out-SimilarityClassSorted.txt

* step5.java: 
	- Outputs pairs of salient nodes (sorted by score in reverse order)
	- Outputs step2-out-SimilarityClassAfterAscending.txt

* step6.java:
	- Identify potential SCUs by checking if each triple of each
	  model summary has at least two salient nodes.
	- Outputs relevant information in step6-out.txt

* step7.java:
	- For each pair of all salient nodes, we find the weight and set of 
	  contributor sentences and contributor triples from each model.
	- For each model summary, we assign weights to its salient triples. 
	- Outputs: 	step7-out-weight.txt
			   	step7-out-PyramidTriple.txt
				step7-out-PyramidSenten.txt
				step7-out-PyramidOnlyWeightForScoring.txt

	- Some notes:	
		* allPhrases is a mapping from a salient node to integer 1.
		
		* allPhrases1 is a mapping from a pair of salient nodes (vertexIterator1, vertexIterator2) 
		to the weight, which is obtained by counting how many model summaries have triples whose
		salient nodes are in the simliarity classes of both vertexIterator1 and vertexIterator2
		
		* allPhrases2forContributor is a mapping from a pair of salient nodes 
		(vertexIterator1, vertexIterator2) to a string that contains all of the relevant 
		information about the contributors triples 
		(such as contributor name, triple from contributor, salient nodes from that triple)
		
		* allPhrases2forContributorSenten is a mapping from a pair of salient nodes 
		(vertexIterator1, vertexIterator2) to a string that contains relevant information 
		about the contributors sentences (contributor name, sentence from contributor summary)

		* g: graph containing all salient nodes as vertices
		* g1: graph containing all neighbors of vertexIterator1 
		     (vertexIterator1 is the similarity class for first salient node)
		* g2: graph containing all neighbors of vertexIterator2 
		     (vertexIterator2 it the similarity class for second salient node)

* step8.java
	- Nearly identical function to step7, just outputs different things.
	- Outputs:	step8-out-contributor.txt
				step8-out-PyramidForCombination.txt
				step8-out-TripSen.txt

* step9.java
	- Combine all the possible labels that come from one sentence 
	  (via calculating the similarity score between two phrases)
	- Outputs:	step9-output-labels.txt

* step10.java
	- Computes connected components of triples in the same sentence using 
	  step-9-output-combineLog.txt
	- Outputs:	step10-out-labels.txt

* step11.java
	- Computes connected components of triples from different sentences using 
	  step-9-output-combineLog.txt
	- Outputs: step11-output-labels.txt

***************************************************************************************************
Scoring the targets: combined.java, HungarianAlgorithm.java
***************************************************************************************************

* scoreTargets.java
	- Contains methods combined(), step12(), step13(), step14(), maxSum(), rpf()
	- combined, step12, step13, step14 are all involved with scoring the summaries
	- maxSum, rpf used to calculate normalized scores

---------------------------------------------------------------------------------------------------
Pyramid Output Files
---------------------------------------------------------------------------------------------------
* step1-out.txt	
	- Format of each line: "vertex1" "vertex2" "similarity between vertex1 and vertex2"
	- vertex1, vertex2 are either S, P, or O parts of triples

* step2-out.txt
	- Format of each line: "vertex1"
	- This output file is used in step3 to count the number of times we encounter a node,
	  and uses this information to determine whether that node is salient

* step2-out-SimilarityClass.txt
	- Format of each line: "similarity between vertex1 and vertex2" "vertex1" "vertex2"

* step2-out-SimilarityClassSorted.txt
	- Simply step2-out-SimilarityClass.txt sorted by similarity (highest to lowest)

* step2-out-SimilarityClassAfterAscending.txt
	- Each line (sorted highest to lowest score): vertex1vertex2

* step3-out.txt
	- Format of each line: salient node

* step6-out.txt
	- Format: 
		#"salientNode1" "salientNode2"
		salient triple 
		FROM: [model name]-# Line X (id X): sentence from model summary


* step7-out-weight.txt
	- Format:
		"salientNode1" "salientNode2" "weight"
		*#"[contributor model name]:" [contributor triple]
		*"salientNode1 from contributor triple" "salientNode2 from contributor triple"
		etc.

* step7-out-PyramidTriple.txt
	- Format:
		[weight] [anchor triple]
		*"salientNode1 from anchor" "salientNode2 from anchor"
		*#"[anchor model name]:" [anchor triple]
		*"salientNode1 from anchor" "salientNode2 from anchor"
		#"[contributor model name]:" [contributor triple]
		*"salientNode1 from contributor" "salientNode2 from contributor"
		etc. 

* step7-out-PyramidSenten.txt
	- Format:
		[weight] [anchor triple]
		FROM: [anchor model name]-# Line X (id X): sentence from anchor model 
		*"salientNode1 from anchor" "salientNode2 from anchor"
		*Sen-[model name 1]: FROM: [model name 1]-# Line Y (id Y): sentence from model1 
		*Sen-[model name 2]: FROM: [model name 2]-# Line Z (id Z): sentence from model2 
		etc.

* step7-out-PyramidOnlyWeightForScoring.txt
	- Format of each line: "weight" [anchor triple]

* step8-out-contributor.txt
	- Format:
		"salientNode1" "salientNode2" "weight"
		*#[contributor name] [contributor triple]
		*"salientNode1 from contributor triple" "salientNode2 from contributor triple" 
		etc.

* step8-out-PyramidForCombination.txt
	- For each model, for each sentence in the model, output line.
	  Then, for each SALIENT triple in that sentence, output salient nodes from the triple, weight, triple. 
	- Format: 
		FROM: [model name]-# Line 1 (id 1): sentence from model summary
		&"salientNode1 from model triple 1, line 1" "salientNode1 from model triple 1, line 1"&[weight]&[model triple 1, line 1]
		&"salientNode1 from model triple 2, line 1" "salientNode1 from model triple 2, line 1"&[weight]&[model triple 2, line 1]
		... (however many triples are in line 1)
		FROM: [model name]-# Line 2 (id 2): sentence from model summary
		&"salientNode1 from model triple 1, line 2" "salientNode1 from model triple 1, line 2"&[weight]&[model triple 1, line 2]
		etc. 

* step8-out-TripSen.txt
	- Intermediate file used to store things needed to produce the debug output in step11

* step9-output-combineLog.txt
	- For each sentence, output the following line for each pair of triples: 
	  &[triple1]&   &[triple2]&   &[similarity]&

	- Similarity = 1 if one triple is a substring of another


* step10-out-labels.txt
	- Each line is an "&"" separated triples followed by the highest weight. 

* step11-out-labels.txt
	- Each line is an "&"" separated triples followed by the highest weight. 

---------------------------------------------------------------------------------------------------
