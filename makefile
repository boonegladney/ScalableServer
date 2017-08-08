all: compile
	@echo -e '[INFO] Done!'
clean:
	@echo -e '[INFO] Cleaning Up..'	
	@-rm -rf cs455/scaling/client/*.class
	@-rm -rf cs455/scaling/server/*.class
	@-rm -rf cs455/scaling/task/*.class
	@-rm -rf cs455/scaling/threadpool/*.class
	@-rm -rf cs455/scaling/util/*.class

compile: 
	@echo -e '[INFO] Compiling the Source..'
	@javac -d . cs455/scaling/client/*.java
	@javac -d . cs455/scaling/server/*.java
	@javac -d . cs455/scaling/task/*.java
	@javac -d . cs455/scaling/threadpool/*.java
	@javac -d . cs455/scaling/util/*.java