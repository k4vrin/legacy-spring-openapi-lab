# Documentation

Start with the [architecture](ARCHITECTURE.md) to see how the WAR starts and where the documentation feature fits. Then follow the lessons in order:

1. [Application context initializers](learning/01-initializers.md): when the callback runs and how this WAR registers it.
2. [Environment and property sources](learning/02-environment.md): how a key is resolved, why source order matters, and what is packaged.
3. [Conditions and startup decisions](learning/03-conditions.md): what `matches` decides and why changing a value later does not rebuild beans.

Each lesson includes a source walkthrough, a small experiment, and recall questions with answers after the questions.

The architecture records why documentation is registered explicitly outside the restricted controller scan, and why the initializer stays separate from the condition. These are project choices with tradeoffs, not requirements imposed by Spring.

- [Integration decisions](DESIGN.md)
- [Executed verification and limits](VERIFICATION.md)
- [Persian LinkedIn draft and carousel](linkedin-post.fa.md)

Run instructions are in the [project README](../README.md). The LinkedIn text is a draft; publishing it on GitHub does not mean it has been posted on LinkedIn.
