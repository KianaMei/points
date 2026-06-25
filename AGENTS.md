# AGENTS.md

## Superpowers Skills

`superpowers` is the GitHub repository name, not a skill name. The installed skills are individual directories under the global Codex skills directory:

`C:\Users\jaqenze\.codex\skills`

After installing or changing skills, restart Codex so the new skill list is loaded.

## How To Use

Invoke a skill explicitly by naming it in the prompt:

```text
$systematic-debugging 修这个 bug
```

```text
用 writing-plans 给这个需求拆实施计划
```

```text
使用 verification-before-completion 检查这次修改是否真的完成
```

Codex may also auto-select a matching skill when the task clearly fits. If a skill conflicts with explicit user or project instructions, the user/project instructions win.

Hard project rules still apply:

- Do not commit git changes unless explicitly asked.
- Do not run a full build unless explicitly asked.
- Do not add AI co-author metadata.

## Club Points Startup

Every time you handle this project, read these first:

1. `docs/startup/00-readme.md`
2. `docs/startup/09-doc-router.md`

Then read at most 1-2 relevant `docs/startup/*` short files for the current task.

Do not read all long design documents by default. Only open the detailed documents listed in `docs/startup/09-doc-router.md` when the short startup files are not enough.

## Installed Superpowers Skills

- `using-superpowers`: Entry workflow for checking and using relevant skills before acting.
- `brainstorming`: Use before creative work such as new features, components, behavior changes, or design exploration.
- `writing-plans`: Use when requirements are known and the task needs a multi-step implementation plan before code changes.
- `executing-plans`: Use when executing an existing written implementation plan with review checkpoints.
- `systematic-debugging`: Use for bugs, test failures, crashes, or unexpected behavior before proposing fixes.
- `test-driven-development`: Use when implementing a feature or bugfix where tests should drive the change.
- `verification-before-completion`: Use before claiming work is complete, fixed, or passing; requires real verification evidence.
- `requesting-code-review`: Use after completing substantial work or before merge-style decisions to verify quality.
- `receiving-code-review`: Use when processing review feedback, especially when feedback is unclear or questionable.
- `finishing-a-development-branch`: Use when implementation is complete and the next step is merge, PR, cleanup, or integration.
- `dispatching-parallel-agents`: Use when there are two or more independent tasks that can run without shared state.
- `subagent-driven-development`: Use when executing implementation plans through independent subagent-sized tasks.
- `using-git-worktrees`: Use when feature work needs an isolated workspace or a separate worktree.
- `writing-skills`: Use when creating, editing, or validating Codex skills.

## Practical Selection Guide

- For a bug: start with `systematic-debugging`.
- For a new feature: start with `brainstorming`, then `writing-plans` if the task is non-trivial.
- For implementing from a plan: use `executing-plans`.
- For test-first work: use `test-driven-development`.
- Before saying something is done: use `verification-before-completion`.
- For review flow: use `requesting-code-review` or `receiving-code-review`.
- For isolated branch/worktree work: use `using-git-worktrees`.
- For many independent subtasks: use `dispatching-parallel-agents` or `subagent-driven-development`.
