#!/bin/bash
for sample in $(ls Halide/tutorial/*.cpp); do
    bName=$(basename ${sample})
    echo ${bName%.cpp}
done
