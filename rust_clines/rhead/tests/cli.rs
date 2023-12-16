use std::process::Output;

use assert_cmd::Command;
use predicates::prelude::*;

const CMD: &str = "head";
const CLONE: &str = "rhead";

const FILES: [&'static str; 3] = [
    "tests/inputs/empty.txt",
    "tests/inputs/one.txt",
    "tests/inputs/ten.txt",
];

#[test]
fn no_args() {
    Command::cargo_bin(CLONE)
        .unwrap()
        .write_stdin("test")
        .assert()
        .success()
        .stdout("test\n");
}

#[test]
fn no_flags() {
    compare(&[]);
}

#[test]
fn lines() {
    for n in ["1", "5", "10"] {
        compare(&["-n", n]);
    }
}

#[test]
fn bytes() {
    for n in ["1", "5", "10"] {
        compare(&["-c", n]);
    }
}

#[test]
fn lines_and_bytes() {
    Command::cargo_bin(CLONE)
        .unwrap()
        .args(&["/tests/inputs/empty.txt", "-n", "10", "-c", "10"])
        .assert()
        .failure()
        .stderr(predicate::str::contains("cannot be used with"));
}

#[test]
fn invalid_file() {
    Command::cargo_bin(CLONE)
        .unwrap()
        .arg("invalid_file.txt")
        .assert()
        .failure()
        .stderr(predicate::str::contains("No such file or directory"));
}

fn compare(flags: &[&str]) {
    for file in FILES {
        print!("{file}");
        let produced = run_command(&mut Command::cargo_bin(CLONE).unwrap(), &[file], flags);
        let expected = run_command(&mut Command::new(CMD), &[file], flags);
        assert_eq!(produced, expected)
    }

    let produced = run_command(&mut Command::cargo_bin(CLONE).unwrap(), &FILES, flags);
    let expected = run_command(&mut Command::new(CMD), &FILES, flags);
    assert_eq!(produced, expected)
}

fn run_command(command: &mut Command, files: &[&str], flags: &[&str]) -> Output {
    command.args(files).args(flags).output().unwrap()
}
