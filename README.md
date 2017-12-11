App Manager
===========
(C) Jesse Gallagher

This application is an early prototype of an idea I had a while ago for a sort of XPages-targetted app manager for Domino. The core idea is that the standard admin tools for Domino don't really do much to assist in managing "app"-y NSFs, and do basically nothing to tell you about XPages in particular, so there are a number of tasks that would benefit from some additional inspection and automation.

The starting itches that needed scratching were:

* General analysis of applications containing XPage elements, to distinguish from old-style or data-only apps
* Overview of XPage-specific requirements, like the libraries used per app
* Checks for potential problems, such as XPage elements signed with multiple signers
* Automated app deployment/update, backing up the current design of an app before applying a new NTF

Grown further, this sort of thing could be very useful, for example by adding a way to POST an NTF to the server to have it inspected for problems and potentially signed and deployed at the end of an automated build process.