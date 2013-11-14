deps:
	lein deps
	mkdir lib
	cp local_jars/*.jar lib/

clean:
	rm -f -r lib/*
