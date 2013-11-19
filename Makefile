deps:
	lein deps
	rm -f -r lib
	mkdir lib
	cp local_jars/*.jar lib/

clean:
	rm -f -r lib/*
