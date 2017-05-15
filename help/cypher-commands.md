## Useful links
* [Sublime Text editor's Package Control - Basic functionality](https://packagecontrol.io/docs/usage)
* [Sublime Text editor's Package Control - Installation](https://packagecontrol.io/installation#st3)
* [Sublime Text editor's Syntax Highlighting for Cypher](https://packagecontrol.io/packages/Cypher)
* [GitHub project - Syntax highlighting for Neo4j's Cypher query language in SublimeText](https://github.com/kollhof/sublime-cypher)

## How to import data from CSV into the Neo4J database

#### Install Neo4j
Install Neo4j on your machine and then go to its install directory and execute bin/neo4j-shell.

#### Importing data from the .CSV file
**Note:** The "*LIMIT 1*" should be removed from the Cypher queries when importing data. 
The sample queries provided below contain it so that they are executed faster (useful when getting familiar with Cypher commands).
  
1. It can be done either by using the URL provided by [Consumer Financial Protection Bureau](https://www.consumerfinance.gov/):
```
LOAD CSV WITH HEADERS 
FROM 'https://data.consumerfinance.gov/api/views/s6ew-h6mp/rows.csv?accessType=DOWNLOAD' AS line 
WITH line 
LIMIT 1 
RETURN line
```

2. Or the .CSV file can be downloaded and imported "offline":
    
In the browser interface (Neo4j 3.0.3, MacOS 10.11) it looks like Neo4j prefixes your file path with *$path_to_graph_database/import*.

**Note:** For Windows users, the *$path_to_graph_database* is usually something like:
 
*c:\Users\<your Windows user>\Documents\Neo4j\default.graphdb*

Therefore you'll have to put all the .csv files here: 

*c:\Users\<your Windows user>\Documents\Neo4j\default.graphdb\import\\*

Typically, you can import data using this command:

```
LOAD CSV WITH HEADERS 
FROM 'file:///Consumer_Complaints.csv' AS line 
WITH line 
LIMIT 1 
RETURN line
```

Some improvements to the previous query:
We will rename the "Date received" column into "date":

```
LOAD CSV WITH HEADERS 
FROM 'file:///Consumer_Complaints.csv' AS line 
WITH line.`Date received` AS date 
LIMIT 1 
RETURN date
```

It's also a good idea to split date columns into day, month, year "fragments":

```
LOAD CSV WITH HEADERS 
FROM 'file:///Consumer_Complaints.csv' AS line 
WITH SPLIT(line.`Date received`, '/') AS date 
LIMIT 1 
RETURN date
```

**Note:** In case something went wrong or you just want to experiment and try different queries you can delete all nodes and relationships with this query:

```
MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n,r
```

Importing data from the .csv into corresponding nodes will be done in several iterations. First, we'll import (and create nodes and relationships for) complaints, companies and responses.

It's mandatory to create unique constraints on complaint IDs, company and response names (needed when merging):  

```
CREATE CONSTRAINT ON (c:Complaint) ASSERT c.id is UNIQUE;
CREATE CONSTRAINT ON (c:Company) ASSERT c.name is UNIQUE;
CREATE CONSTRAINT ON (r:Response) ASSERT r.name is UNIQUE;
```

Check if the constraints have successfully been created using the following command:
```
:schema
```

Continue with the following Cypher query to create Company, Complaint and Response nodes and their relationships:

```
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
	r.disputed = CASE line.`Consumer disputed?` WHEN 'Yes' THEN true ELSE false END;
```

For the second iteration, we'll continue with Product and Issue nodes and their relationships.

**Note 1:** We'll be using "match" instead of "create" because Complaint nodes already exist in our Neo4j database.

**Note 2:** Uncomment this line: "WITH line, SPLIT(line.`Date received`, '/') AS date " in case you plan to uncomment the "LIMIT 10" line as well (for testing purposes).

Again, the very first thing to do is to create the unique constraints:
```
CREATE CONSTRAINT ON (p:Product) ASSERT p.name is UNIQUE;
CREATE CONSTRAINT ON (i:Issue) ASSERT i.name is UNIQUE;
```

Then:
```
USING PERIODIC COMMIT 1000
LOAD CSV WITH HEADERS FROM 'file:///Consumer_Complaints.csv' AS line 

//WITH line, SPLIT(line.`Date received`, '/') AS date 
//LIMIT 10

MATCH (complaint:Complaint {id: TOINT(line.`Complaint ID`) })

MERGE (product: Product {name: UPPER(line.Product)})
MERGE (issue: Issue {name: UPPER(line.Issue)})

CREATE (complaint)-[:ABOUT]->(product)
CREATE (complaint)-[:WITH]->(issue);
```

#### Issues you may encounter
During .CSV import you may encounter this kind of error:

```
At C:\Users\itsix\Documents\Neo4j\default.graphdb\import\Consumer_Complaints.csv:956356 -  there's a field starting with a quote and whereas it ends that quote there seems to be characters in that field after that ending quote. That isn't supported. This is what I read: 'Shellpoint Mortgage Service XX/XX/2016 ATTN EXCALLATION DEPARTEMNT LOAN NUMBER XXXX XXXX, Chief of the Escalation Department. 

Dear XXXX : I am in receipt of the letter send from you Department on XX/XX/2016- Someone is CLEARY NOT PAYING ATTENTION TO MY ACCOUNT : OR THE RETENTION OF MY HOME- When I received the call on XX/XX/2016- from XXXX he knew that the DOLLARS where incorrect on my Modification Denial with the UNDERWRITING DEPARTMENT not adding any rental income. In addition, his superior called me per my insistence in the Denial of the Modification an also concurred that there was no rental income and the Modification showing a NEGATIVE loan ratio of ( -140 % ) -- Debit to income- NOW let 's look at page one of your XX/XX/XXXX letter FROT with ERRORS- and misinformation. 
# 1 My net income for EXCURSIONS as sent in is $ XXXX That is my SEDAN CAR BUISNESS No added correctly is the long term rental property income of {$5300.00} discounting the income per HAMP at 75 % is a positive {$3900.00}. THAT would clearly make a monthly NET income to me of {$6500.00} per month NET to ME XXXX- take away the MORTGAGE PAYMENT for the rental unit of {$2600.00} = {$3900.00} take away with a new HAMP MODIFIED PAYMENT on the balance of {$370000.00} on a 2 % note the payment would be at {$1300.00} per month- plus taxes and insurance of roughly {$500.00} per month I would be at 50 % LOAN TO VALUE NOT NEGATIVE 140 PERCENT- SO, let 's look at {$6500.00} a payment of {$1900.00} would be a positive 32 % Debt to income ratio- Now please try to explain this to the Consumer Protection Bureau- your response is not posted on my site and a second note was sent to you for my concerns of your response. 

XXXX CO XXXX- XXXX CC XXXX Attorney on File and the Consumer Protection Bureau. 
",Company believes it acted appropriately as authorized by contract or law,"S'
```
This means some minor manual corrections have to be made to the .CSV file in order to remove or change those special characters causing the problems.
It's a small trade-off anyway, as I noticed there aren't too many places where this problem occurs.

#### Extract SubIssue and SubProduct data into two separate .CSV files
This is because not all the rows in the original .CSV file have data filled in for SubIssue or SubProduct and these rows must be filtered out.

Steps to take here: 
1. Copy the SubIssue and Complaint ID content into a new .CSV file (only these two columns from the original file), remove the rows where there is no data filled in for SubIssue (cell value is empty).
2. Copy the SubProduct and Complaint ID content into a new .CSV file (only these two columns from the original file), remove the rows where there is no data filled in for SubProduct (cell value is empty).
3. Then, in a similar fashion, the SubIssue and SubProduct nodes and relationships will be created.

## Open items/questions

1. How to make bin/neo4j-shell work in Windows (cypher-shell.bat)?

2. FIXED (see above) - How to use file:// in Windows?

## Complete import script

```
CREATE CONSTRAINT ON (c:Complaint) ASSERT c.id is UNIQUE;
CREATE CONSTRAINT ON (c:Company) ASSERT c.name is UNIQUE;
CREATE CONSTRAINT ON (r:Response) ASSERT r.name is UNIQUE;
```

```
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
	r.disputed = CASE line.`Consumer disputed?` WHEN 'Yes' THEN true ELSE false END;
```

```
CREATE CONSTRAINT ON (p:Product) ASSERT p.name is UNIQUE;
CREATE CONSTRAINT ON (i:Issue) ASSERT i.name is UNIQUE;
```

```
USING PERIODIC COMMIT 1000
LOAD CSV WITH HEADERS FROM 'file:///Consumer_Complaints_corrected.csv' AS line 

MATCH (complaint:Complaint {id: TOINT(line.`Complaint ID`) })

MERGE (product: Product {name: UPPER(line.Product)})
MERGE (issue: Issue {name: UPPER(line.Issue)})

CREATE (complaint)-[:ABOUT]->(product)
CREATE (complaint)-[:WITH]->(issue);
```

```
CREATE CONSTRAINT ON (s:SubIssue) ASSERT s.name is UNIQUE;
```

```
USING PERIODIC COMMIT
LOAD CSV WITH HEADERS FROM 'file:///Consumer_Complaints_corrected_subissues.csv' AS line 

MATCH (complaint:Complaint {id: TOINT(line.`Complaint ID`) })
MATCH (complaint)-[:WITH]->(issue:Issue)

MERGE (subissue:SubIssue {name: UPPER(line.`Sub-issue`)})

CREATE (complaint)-[:WITH]->(subissue)
MERGE (subissue)-[:IN_CATEGORY]->(issue);
```

```
CREATE CONSTRAINT ON (s:SubProduct) ASSERT s.name is UNIQUE;
```

```
USING PERIODIC COMMIT
LOAD CSV WITH HEADERS FROM 'file:///Consumer_Complaints_corrected_subproducts.csv' AS line 

MATCH (complaint:Complaint {id: TOINT(line.`Complaint ID`) })
MATCH (complaint)-[:ABOUT]->(product:Product)

MERGE (subproduct:SubProduct {name: UPPER(line.`Sub-product`)})

CREATE (complaint)-[:ABOUT]->(subproduct)
MERGE (subproduct)-[:IN_CATEGORY]->(product);
```

## Sample Cypher command to query for data

#### Retrieve all details given an existing Complain ID (change the ID to match the data in your DB)
```
MATCH (complaint:Complaint) 
MATCH (complaint)<-[:TO]-(response:Response)
MATCH (complaint)-[:WITH]->(issue:Issue)
MATCH (complaint)-[:ABOUT]->(product:Product)

WHERE ID(complaint)=1590361 

OPTIONAL MATCH (complaint)-[:WITH]->(subIssue:SubIssue)-[:IN_CATEGORY]->(issue)
OPTIONAL MATCH (complaint)-[:ABOUT]->(subProduct:SubProduct)-[:IN_CATEGORY]->(product)

RETURN complaint, issue, subIssue, product, subProduct, response
```

#### Sample query to return complaint, product and sub product
```
MATCH (c:Complaint)-[:ABOUT]->(p:Product)<-[:IN_CATEGORY]-(sp:SubProduct)<-[:ABOUT]-(c) RETURN c limit 1
```

#### Sample query to return complaint, issue and sub issue
```
MATCH (c:Complaint)-[:WITH]->(i:Issue)<-[:IN_CATEGORY]-(si:SubIssue)<-[:WITH]-(c) RETURN c limit 1
```