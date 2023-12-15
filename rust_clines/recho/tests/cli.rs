use assert_cmd::Command;
use predicates::prelude::*;

const CMD: &str = "recho";

#[test]
fn no_args() {
    Command::cargo_bin(CMD)
        .unwrap()
        .arg("-n")
        .assert()
        .success()
        .stdout(predicate::str::is_empty());
}

#[test]
fn one_arg() {
    run(&["hello"], "hello\n");
}

#[test]
fn multiple_args() {
    run(&["hello", "world"], "hello world\n");
}

#[test]
fn no_newline() {
    run(&["hello", "world", "-n"], "hello world");
}

fn run(args: &[&str], pattern: &str) {
    Command::cargo_bin(CMD)
        .unwrap()
        .args(args)
        .assert()
        .success()
        .stdout(pattern.to_owned());
}
