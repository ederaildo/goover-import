[![Goover Logo](https://github.com/ederaildo/goover-services/blob/master/goover_logo.jpg?raw=true)](http://www.gooverapp.com/)


#Goover Import

## About

Goover is an mobile app that indicates trendig for TV series, TV Shows and Internet Streams.

Goover Import is an application like uber-jar that imports TV Shows for Goover Backend.

## What´s Import does?

The import app dows follow actions:

* Read a tv shows list inputed in a text file `listimport.txt`
* Read the properties files with database configurations, tv shows categories and where the show is transmitted.
* For each show in the list the application gets from API service [http://api.themoviedb.org](http://api.themoviedb.org) this API returns a JSON REST with information of TV Show like title, genre, sinopsis, poster image etc.
* Is perfomed a local temporary upload for tv show image and them upload in [Cloudinary.com](www.cloudinary.com).
* These informations are inputed in JSON document and follow inserted in database.

## Pre Requisites

Installing and run you should follows tools installed:

* [MongoDB](www.mongodb.org) 
* [Java 1.8](www.java.com)
* [Maven](www.maven.apache.org)

## Installation

Goover Import was build onver `.project` [Eclipse](www.eclipse.org).
So it´s only necessary import the Eclipse project to local workspace.

## Structure

Directory structure of application:

```
/gooverimport
__/conf
__/file
__/img
__/log
__gooverimport.jar
```

### Directory and resources description:


#### Directory ```/conf```

Directory with all application configuration. This directory has 2 files:


* `config.properties`

Propriety File with follow informations:

* MongoDB Database URL
* Broadcasters IDs (for example: HBO, ABC, CBS, YouTube, Netflix etc.). That´s itens are separated by comma and without spaces. Example: `5768a4b80b12fbe73f0c28a1,5768a4b80b12fbe73f0c2832`

* Categories IDs also separated by comma. Examplo:
`categorias=576896fb6d4a0f3c01e182cd,5768977a6d4a0f3c01e182cf`

* Cloudinary API Key and The Movie DB Api



* `veiculos.txt`

Inside the directory has a file named `veiculos.txt` this file is a consulting guide for discovery broadcasters IDs and them configurate `config.properties` file.


	

#### Directory `/file`

File `listimport.txt`

This file will contains a list of brodacasters names that´s will be imported to Goover Backend
The format is a simple list, for example:

```
Game Of Thrones
Parade's End
Penny Dreadful
```

#### Directory `/img`

Temporary image file. (doesn´t action here)


#### Directory `/log`

Import logs files.

## Run

Run the simple command:

```
java -jar gooverimport.jar
```

## Logs Files:

The logs files contains information of action perfomed by application like database connection, tv show imported and others informations.
At the end of log will be generated a little statistic report:

```
----------------------------------------------
---------------   Statistics   ---------------
----------------------------------------------
-- Shows not found ---------------------------
The Walking Dead
Legion
-- Show without genres -----------------------
Grey´s Anatomy
-- Show without poster image -----------------
Dios Inc.
-- Show without sinopsis ---------------------
Two and a half men
----------------------------------------------
```


## Contributors



## License
