// Useful links
// https://packagecontrol.io/packages/Cypher
// https://packagecontrol.io/installation#st3
// https://packagecontrol.io/docs/usage
// https://github.com/kollhof/sublime-cypher

Go to Neo4j install directory and execute bin/neo4j-shell

LOAD CSV WITH HEADERS FROM 'https://data.consumerfinance.gov/api/views/s6ew-h6mp/rows.csv?accessType=DOWNLOAD' AS line WITH line LIMIT 1 RETURN line

// In the browser interface (Neo4j 3.0.3, MacOS 10.11) it looks like Neo4j prefixes your file path with $path_to_graph_database/import.
// Windows: put all the .csv files here: c:\Users\itsix\Documents\Neo4j\default.graphdb\import\
LOAD CSV WITH HEADERS 
FROM 'file:///Consumer_Complaints.csv' AS line 
WITH line 
LIMIT 1 
RETURN line

LOAD CSV WITH HEADERS FROM 'file:///Consumer_Complaints.csv' AS line WITH line.`Date received` AS date LIMIT 1 RETURN date
LOAD CSV WITH HEADERS FROM 'file:///Consumer_Complaints.csv' AS line WITH SPLIT(line.`Date received`, '/') AS date LIMIT 1 RETURN date


// How to delete all nodes and relationships:
MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n,r

//********************* Start

// first iteration - Complaints, companies and responses

CREATE CONSTRAINT ON (c:Complaint) ASSERT c.id is UNIQUE;
CREATE CONSTRAINT ON (c:Company) ASSERT c.name is UNIQUE;
CREATE CONSTRAINT ON (r:Response) ASSERT r.name is UNIQUE;

//check using 
:schema

// continue with: Company, Complaint and Response

USING PERIODIC COMMIT 1000
LOAD CSV WITH HEADERS FROM 'file:///Consumer_Complaints.csv' AS line 
WITH line, SPLIT(line.`Date received`, '/') AS date 

//LIMIT 10

CREATE (complaint:Complaint {id: TOINT(line.`Complaint ID`) })
SET complaint.year = TOINT(date[2]),
	complaint.month = TOINT(date[0]),
 	complaint.day = TOINT(date[1])

MERGE (company: Company {name: UPPER(line.Company) })
MERGE (response: Response {name: UPPER(line.`Company response to consumer`) })

CREATE (complaint)-[:AGAINST]->(company)
CREATE (response)-[r:TO]->(complaint)

SET r.timely = CASE line.`Timely response?` WHEN 'Yes' THEN true ELSE false END,
	r.disputed = CASE line.`Consumer disputed?` WHEN 'Yes' THEN true ELSE false END
;

// second iteration - continue with: Product and Issue
//Note 1: match instead of create - nodes already exist in DB
//Note 2: keep this line "WITH line, SPLIT(line.`Date received`, '/') AS date " so that "LIMIT 10" works
CREATE CONSTRAINT ON (p:Product) ASSERT p.name is UNIQUE;
CREATE CONSTRAINT ON (i:Issue) ASSERT i.name is UNIQUE;

USING PERIODIC COMMIT 1000
LOAD CSV WITH HEADERS FROM 'file:///Consumer_Complaints.csv' AS line 

//WITH line, SPLIT(line.`Date received`, '/') AS date 
//LIMIT 10

MATCH (complaint:Complaint {id: TOINT(line.`Complaint ID`) })

MERGE (product: Product {name: UPPER(line.Product)})
MERGE (issue: Issue {name: UPPER(line.Issue)})

CREATE (complaint)-[:ABOUT]->(product)
CREATE (complaint)-[:WITH]->(issue)
;

//************************ Open questions
1. How to make bin/neo4j-shell work in Windows (cypher-shell.bat)?
2. FIXED (see above) - How to use file:// in Windows?















//*****************************************************
CREATE CONSTRAINT ON (c:Complaint) ASSERT c.id is UNIQUE;
CREATE CONSTRAINT ON (c:Company) ASSERT c.name is UNIQUE;
CREATE CONSTRAINT ON (r:Response) ASSERT r.name is UNIQUE;


USING PERIODIC COMMIT 1000
LOAD CSV WITH HEADERS FROM 'file:///Consumer_Complaints_corrected.csv' AS line 
WITH line, SPLIT(line.`Date received`, '/') AS date 

CREATE (complaint:Complaint {id: TOINT(line.`Complaint ID`) })
SET complaint.year = TOINT(date[2]),
	complaint.month = TOINT(date[0]),
 	complaint.day = TOINT(date[1])

MERGE (company: Company {name: UPPER(line.Company) })
MERGE (response: Response {name: UPPER(line.`Company response to consumer`) })

CREATE (complaint)-[:AGAINST]->(company)
CREATE (response)-[r:TO]->(complaint)

SET r.timely = CASE line.`Timely response?` WHEN 'Yes' THEN true ELSE false END,
	r.disputed = CASE line.`Consumer disputed?` WHEN 'Yes' THEN true ELSE false END
;





CREATE CONSTRAINT ON (p:Product) ASSERT p.name is UNIQUE;
CREATE CONSTRAINT ON (i:Issue) ASSERT i.name is UNIQUE;


USING PERIODIC COMMIT 1000
LOAD CSV WITH HEADERS FROM 'file:///Consumer_Complaints_corrected.csv' AS line 

MATCH (complaint:Complaint {id: TOINT(line.`Complaint ID`) })

MERGE (product: Product {name: UPPER(line.Product)})
MERGE (issue: Issue {name: UPPER(line.Issue)})

CREATE (complaint)-[:ABOUT]->(product)
CREATE (complaint)-[:WITH]->(issue)
;






CREATE CONSTRAINT ON (s:SubIssue) ASSERT s.name is UNIQUE;


USING PERIODIC COMMIT
LOAD CSV WITH HEADERS FROM 'file:///Consumer_Complaints_corrected_subissues.csv' AS line 

MATCH (complaint:Complaint {id: TOINT(line.`Complaint ID`) })
MATCH (complaint)-[:WITH]->(issue:Issue)

MERGE (subissue:SubIssue {name: UPPER(line.`Sub-issue`)})

CREATE (complaint)-[:WITH]->(subissue)
MERGE (subissue)-[:IN_CATEGORY]->(issue)
;





CREATE CONSTRAINT ON (s:SubProduct) ASSERT s.name is UNIQUE;


USING PERIODIC COMMIT
LOAD CSV WITH HEADERS FROM 'file:///Consumer_Complaints_corrected_subproducts.csv' AS line 

MATCH (complaint:Complaint {id: TOINT(line.`Complaint ID`) })
MATCH (complaint)-[:ABOUT]->(product:Product)

MERGE (subproduct:SubProduct {name: UPPER(line.`Sub-product`)})

CREATE (complaint)-[:ABOUT]->(subproduct)
MERGE (subproduct)-[:IN_CATEGORY]->(product)
;



//leads to this error:
At C:\Users\itsix\Documents\Neo4j\default.graphdb\import\Consumer_Complaints.csv:956356 -  there's a field starting with a quote and whereas it ends that quote there seems to be characters in that field after that ending quote. That isn't supported. This is what I read: 'Shellpoint Mortgage Service XX/XX/2016 ATTN EXCALLATION DEPARTEMNT LOAN NUMBER XXXX XXXX, Chief of the Escalation Department. 

Dear XXXX : I am in receipt of the letter send from you Department on XX/XX/2016- Someone is CLEARY NOT PAYING ATTENTION TO MY ACCOUNT : OR THE RETENTION OF MY HOME- When I received the call on XX/XX/2016- from XXXX he knew that the DOLLARS where incorrect on my Modification Denial with the UNDERWRITING DEPARTMENT not adding any rental income. In addition, his superior called me per my insistence in the Denial of the Modification an also concurred that there was no rental income and the Modification showing a NEGATIVE loan ratio of ( -140 % ) -- Debit to income- NOW let 's look at page one of your XX/XX/XXXX letter FROT with ERRORS- and misinformation. 
# 1 My net income for EXCURSIONS as sent in is $ XXXX That is my SEDAN CAR BUISNESS No added correctly is the long term rental property income of {$5300.00} discounting the income per HAMP at 75 % is a positive {$3900.00}. THAT would clearly make a monthly NET income to me of {$6500.00} per month NET to ME XXXX- take away the MORTGAGE PAYMENT for the rental unit of {$2600.00} = {$3900.00} take away with a new HAMP MODIFIED PAYMENT on the balance of {$370000.00} on a 2 % note the payment would be at {$1300.00} per month- plus taxes and insurance of roughly {$500.00} per month I would be at 50 % LOAN TO VALUE NOT NEGATIVE 140 PERCENT- SO, let 's look at {$6500.00} a payment of {$1900.00} would be a positive 32 % Debt to income ratio- Now please try to explain this to the Consumer Protection Bureau- your response is not posted on my site and a second note was sent to you for my concerns of your response. 

XXXX CO XXXX- XXXX CC XXXX Attorney on File and the Consumer Protection Bureau. 
",Company believes it acted appropriately as authorized by contract or law,"S'


MATCH (complaint:Complaint) 
MATCH (complaint)<-[:TO]-(response:Response)
MATCH (complaint)-[:WITH]->(issue:Issue)
MATCH (complaint)-[:ABOUT]->(product:Product)

WHERE ID(complaint)=1590361 

OPTIONAL MATCH (complaint)-[:WITH]->(subIssue:SubIssue)-[:IN_CATEGORY]->(issue)
OPTIONAL MATCH (complaint)-[:ABOUT]->(subProduct:SubProduct)-[:IN_CATEGORY]->(product)

RETURN complaint, issue, subIssue, product, subProduct, response


//sample query to return complaint, product and sub product
MATCH (c:Complaint)-[:ABOUT]->(p:Product)<-[:IN_CATEGORY]-(sp:SubProduct)<-[:ABOUT]-(c) RETURN c limit 1

//sample query to return complaint, issue and sub issue
MATCH (c:Complaint)-[:WITH]->(i:Issue)<-[:IN_CATEGORY]-(si:SubIssue)<-[:WITH]-(c) RETURN c limit 1