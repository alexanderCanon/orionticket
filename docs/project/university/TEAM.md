# Who does what
  └── person → services they own

# How we work
  └── branching strategy
  └── PR review rules
  └── definition of done

# How to run the project locally
  └── one command setup

# Sprint board
  └── link to Trello/GitHub Project

# Where to find everything
  └── documentation index

Each person owns specific bounded contexts end-to-end — backend, tests, and their piece of the frontend. No shared ownership, no coordination overhead on the same file.
With 4 people a clean split could be:
PersonOwns1Identity + Event Management + organizer panel2Seating/Inventory + Orders + Payments3Ticket Issuance + Notifications + buyer portal4Access Control + Reporting + DevOps + deployment