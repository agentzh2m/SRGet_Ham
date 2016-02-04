# This is the speedy resumable Concurrent Get HAMUEL version

this version like to download cute cats JPG from the internet you can download a lot of cute cats if you want to 
my Thread worker is a cat it is lazy and hard working at times :)

usage: run in the directory that is file is in  ./srget.sh -o <output> -c <number of connection> <url>
 
The script is updated you can now run the script srget therefore you can call

srget -o Hamuel.xyz http://www.hamuel.com YEH!! nice binary source in "Out"

for concurrent connection you can do something below

srget -o Hamuel.xyz -c 5 http://www.ham.net.com 

Note: The Binary file can be run with JVM 1.8++ (install JVM before using it)
warning the link and URL are fictional please take precaution when you are using it
--------------------------------
# How it work in case you are not interested in Cute Cat
    For Single Downloaded Connection
        1. Verfied the Header using chkDL class
        2. When it is Verfied start download with mainDL.newDL()
        3. Keep the header as FILENAME.HEAD and data as FILENAME.DATA (both of them is a normal file of binary data)
        4. If a "cat" trips the wire all the info is save in .HEAD and .DATA
        5. Resume by reading content Length in .HEAD and compare the Etag and stuff from that file
        6. When finish rename and delete .HEAD YEH!!
    
    For Concurrent Download Connection
        1. Same as normal download
        2. MUST HAVE content length to download if there is no content length switch to 
        single download connection mainDL.newDL()
        3. Before starting the download split the file into chunks of 1 MB 
        4. Assign "Cats" (Worker) from the pool of "cats" (Threads) to start working on the chunks when the cat 
        finish working it go back to the pool of cats to wait for the next job (hope the cat don't get too lazy!!)
        5. The "Cat" track each chunk into my serialize class, I write the serialize class constanly to make sure
        the Cat don't miscount the byte they work on (Implementation with 2 Array List startPOS and endPOS and use 
        the index of that to track the chunks)
        6. When a dog eat the wires we can still resume because we track how much we write to the file and we can fill in the
        gap with our startPOS and endPOS list
        7. When resume we assing the Cat to fill in the Gap "correctly"
        8. When finish delete FILENAME.HEADC and rename FILENAME.DATAC to the correct FILENAME to tell the user that it is finish
        
    Features Supported:
        1. Multiple Concurrent download
        2. HTTP Code Error Handling such as 404
        3. Redirect support (recursively)
        4. Can resume with any amount of connection you like
        5. Can open infinitely amount of connection but less than the number of Threads on your CPU 
        6. Downloading Progress Indicator In percentage
        7. Tell the user what we are currently doing 
    
    Features not supported:
        1. When download CTE file the file will be corrupted (so not supported oops)
        2. Cannot resume from normal downloaded file due to the different implementation between the two system
        but you can open one connection to download -c 1 and you can resume with -c 2
----------

#For ICCS223 Information

by: Worapol Boontanonda (5780431)

Libraries used: java.nio, java.io, java.net, java.util

(It will be good if I have more time but this is the best I can do)

