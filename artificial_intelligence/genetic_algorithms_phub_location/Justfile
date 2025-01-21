generate := `mktemp`
resources := "src/main/resources"
or-library := resources / "or-library"

# Runs the project with whatever problem is set by default
run ARGS:
  @mvn -q compile exec:java -Dexec.mainClass="uimp.muia.rpm.Run" -Dexec.args="{{ARGS}}"

# Executes the benchmark for max evaluations
optimal:
  @mvn -q compile exec:java -Dmaven.compiler.compilerArgs="-g:none -XX:+AggresiveOpts" -Dexec.mainClass="uimp.muia.rpm.OptimalSearchBenchmark"

# Executes the benchmark for max evaluations
max_evals:
  @mvn -q compile exec:java -Dmaven.compiler.compilerArgs="-g:none -XX:+AggresiveOpts" -Dexec.mainClass="uimp.muia.rpm.MaxEvaluationsBenchmark"

# Builds the generate.c executable
build_gen:
  @gcc {{or-library}}/generate.c -o {{generate}}
  @chmod +x {{generate}}

# Generates a new subproblem with the given N and P
subproblem N P: build_gen
  @{{generate}} {{N}} {{P}} < {{or-library}}/APdata200.txt > {{resources}}/subproblems/phub_{{N}}.{{P}}.txt

# Runs the project in debug mode
debug $MAVEN_OPTS="-ea":
  @mvn compile exec:java -Dexec.mainClass="uimp.muia.rpm.Run" -Dexec.args="-ea" -Dorg.slf4j.simpleLogger.defaultLogLevel=TRACE
  
clean:
  @rm out/*
  @rm target/*
