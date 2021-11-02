# dtype-next-workshop-starter

This is a repository template for the re:Clojure / SciCloj workshop
"Wrangling Arrays with dtype-next", given in association with the
[2021 re:Clojure conference](https://www.reclojure.org/#speakers), and
developed by [David Sletten]() and [Ethan
Miller](http://ethanzanemiller.com)

## Workshop description

This workshop will introduce dtype-next, explain its position in the
Clojure data science ecosystem, and introduce the key concepts and
techniques necessary for working with its performant buffers/arrays.

## What is this Template Repository?

This is a github template repository. That means you can use this
repository to create a new repository of your own in order to play
around with dtype-next further. See
[here](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template)
for a guide on how to create a repository from this template.


What is in this repository?

* A `deps.edn` file containing the dependencies you need to work with
  dtype-next. Really this is just one dependence: the
  [`tech.ml.dataset`](https://github.com/techascent/tech.ml.dataset)
  library -- that provides a column-based dataset for data analysis --
  and which includes the latest stable version of dtype-next.

* A namespace for starting your own exploration that already includes
  typical require statements for dtype-next, i.e.
  `src/playground/main.clj`.
  
* The code that we will cover in our workshop, see the
  `src/workshop/*`.




