use std::error::Error;

use clap::Parser;

pub type RunResult = Result<(), Box<dyn Error>>;

pub trait CommandClone<T: Parser> {
    fn run() -> RunResult {
        let args = T::parse();
        Self::run_with_args(args)
    }

    fn run_with_args(args: T) -> RunResult;
}
