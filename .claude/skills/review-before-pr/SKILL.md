---
name: review-before-pr
description: Use after finishing the implementation of a ticket and before committing, creating a pull request, or reporting the work as done. Runs an independent review.
---

# Review before PR

The work is not done until an independent reviewer has seen it.

1. Confirm the full build passes: `./mvnw clean verify` across all modules.
2. Delegate the review to the `code-reviewer` subagent. Give it only the ticket
   key and the base branch. Do not give it your reasoning, your summary of the
   change, or your justification for any decision — it must reach its own
   conclusions from the diff, the ticket and the code.
3. Treat every finding as an unverified candidate. Re-open the cited code and
   confirm reachability before accepting it. Reject findings that fail the
   reviewer's own finding gate.
4. For each accepted finding, make the smallest fix, then re-run the affected
   tests. A changed fix invalidates the earlier review evidence for that path.
5. Report to the owner: the reviewer's RESULT, the findings you accepted and
   fixed, the ones you rejected and why, and the UNCHECKED_SURFACE.

Never skip this because the change looks small. Never self-approve by claiming
the review would find nothing.