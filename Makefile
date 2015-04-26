all: compile

compile:
	mvn compile -q

run:
	mvn exec:exec -q
