# Linda Tuple Space Part I

Submitted by: Yu-Cheng Lin

## User Stories

Linda space is a distributed data structures, which consists of a collection of logical tuples called tuplespace.

### For users 
They can put their data, (1, 
"Owen", "213-631-XXXX") for example, into shared tuple space, and retrieved back at anytime.
 
Here are some action supported:

"OUT" means user can put tuples into the tuplespace.

"RD" means user can read tuples in the the tuplesapce if existed; otherwise, they have to wait for the data (blocking call).

"IN" means user can read the tuple as wells as remove it from the tuplespace. This also an blocking call if there is no matched tuple.

"? variableName : type" means user can search for specific type of tuple. This also an blocking call if there is no matched tuple.

Note: This work is mainly for transparency, which means user don't know how the implementations.

### For administrator
They can "ADD" or "DELETE" the server. 

Note: Part I of this work implement the basic functions of Linda, and part II supports redundancy and fault-tolerant.

## System Overview


## More Info

This work based on JAVA socket programming and multithreading.
