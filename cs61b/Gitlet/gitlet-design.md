# Gitlet Design

# Classes and Data Structures
## **Blobs**

This class represents an individual file.

## Fields
1. SHA1: the unique ID of a blob
2. content: the content of the file
3. name: the name of the file


## **Commits**

This class represents a specific commit made in the current directory.

## Fields
1. message: a message associated with this commit
2. date: the specific time when the commit is made
3. parentCommit: the previous commit. It should be recorded as a hash code, and the very initial commit has null.
4. trackedBlobs: a series of blobs that are tracked by this commit


## **Repository**

This class represents a repository. It contains a staging area that allows files to be added. It also maintains a mapping of all branches of commits.

## Fields
1. branches: a map that maps branch name to the first commit ID in the branch
2. current branch: points to the currently active branch
3. staging: a path to the directory that stores all added files


## **Command**

This is the abstract class for all of the commands that can be executed in Gitlet.

## Fields
1. args: gives the specific command that needs to be run
2. repository: gives the repository which this command would operate in



# Algorithms
## Commit Class
1. commit(): the class constructor. It would create a new commit object which allow files to be added or to be removed for tracking. The constructor should set up all the necessary metadata(log message, timestamp, parent commit, etc.) and then create a new map for current staged files. It should then have methods that would add or remove staged files to the current commit/from the current commit.


## Repository Class
1. repository(): the class constructor should create a new directory for the current repository. It should contain an initial commit and a current branch called master that points to the initial commit. 
2. addBranch: allows for new branches to be added
3. updateBranch: update the currently active branch to the branch name passed in
4. updateFile: update the content of a file in the repository


## Commands Class
1. run(): this should be an abstract class which gives the frame for different commands. Concrete class of specific commands have to implement the apply() method to actually execute the commands. This would include following different commands:
- commit
- add
- rm
- log
- global-log
- find 
- status
- checkout
- branch
- rm-branch
- reset
- merge


# Persistence

In order to persist the state of a repository, we would need to save the state of the repository after each command executed. To do so, we need to:

1. Write the commit tree to disk. We can serialize them into bytes that we can eventually write to a specially named file on disk. This can be done with writeObject method from the Utils class.
2. Write the repository to disk after each call to the main method. We can serialize the repository object into bytes that we can eventually write a named file on disk. This can be done with writeObject method from the Util class.

In order to retrieve our state, before executing any code, we need to search for the saved files in the working directory (folder in which our program exists) and load the objects that we saved in them. Since we set on a file naming convention (“staging”, etc.) our program always knows which files it should look for. We can use the readObject method from the Utils class to read the data of files as and deserialize the objects we previously wrote to these files.



