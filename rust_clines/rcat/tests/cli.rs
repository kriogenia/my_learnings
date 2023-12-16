use std::process::Output;

use assert_cmd::Command;
use predicates::prelude::*;

const CMD: &str = "rcat";

const SINGLE_TXT: &str = "tests/inputs/single_line.txt";
const MULTI_TXT: &str = "tests/inputs/multi_line.txt";

#[test]
fn no_args() {
    Command::cargo_bin(CMD)
        .unwrap()
        .write_stdin("test")
        .assert()
        .success()
        .stdout("test\n");
}

#[test]
fn single_line_file() {
    compare(&[SINGLE_TXT]);
}

#[test]
fn multi_line_file() {
    compare(&[MULTI_TXT]);
}

#[test]
fn multiple_files() {
    compare(&[SINGLE_TXT, MULTI_TXT])
}

#[test]
fn invalid_file() {
    Command::cargo_bin(CMD)
        .unwrap()
        .arg("invalid_file.txt")
        .assert()
        .failure()
        .stderr(predicate::str::contains("No such file or directory"));
}

fn compare(file_paths: &[&str]) {
    let mut rcat = Command::cargo_bin(CMD).unwrap();
    let mut cat = Command::new("cat");

    for flag in vec![None, Some("-n"), Some("-b")].iter() {
        let produced = run(&mut rcat, file_paths, flag);
        let expected = run(&mut cat, file_paths, flag);
        assert_eq!(produced, expected)
    }
}

fn run(command: &mut Command, file_paths: &[&str], flag: &Option<&str>) -> Output {
    if let Some(flag) = flag {
        command.arg(flag);
    }

    command.args(file_paths).output().unwrap()
}
