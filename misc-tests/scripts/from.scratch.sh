#!/bin/bash
#
# Assuming that git, cmake, gcc, g++, and make are installed and available.
#
#
function die () {
    echo $1
    exit 1;
}

echo -en "Do we clone the repos ? y|[n] > "
read REPLY
if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]; then
    echo -e "Skipping clone step"
else
    # Clone Halide
    echo -e "Cloning Halide"
    git clone https://github.com/halide/Halide.git || die "Halide clone failed"
    # Clone LLVM (Low Level Virtual Machine)
    echo -e "Cloning LLVM"
    git clone --depth 1 --branch llvmorg-13.0.0 https://github.com/llvm/llvm-project.git || die "LLVM clone failed"
    # 
fi
#
echo -en "Do we proceed to make ? y|[n] > "
read REPLY
if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]; then
    echo -e "Skipping make step"
else
    echo -e "Make, step 1"
    cmake -DCMAKE_BUILD_TYPE=Release \
        -DLLVM_ENABLE_PROJECTS="clang;lld;clang-tools-extra" \
        -DLLVM_TARGETS_TO_BUILD="X86;ARM;NVPTX;AArch64;Mips;Hexagon;WebAssembly" \
        -DLLVM_ENABLE_TERMINFO=OFF \
        -DLLVM_ENABLE_ASSERTIONS=ON \
        -DLLVM_ENABLE_EH=ON \
        -DLLVM_ENABLE_RTTI=ON \
        -DLLVM_BUILD_32_BITS=OFF \
        -S llvm-project/llvm \
        -B llvm-build                      || die "Make step 1 failed"
    echo -e "Make, step 2 (it's a long one, be patient)"
    cmake --build llvm-build || die "Make step 2 failed"
    echo -e "Make, step 3"
    cmake --install llvm-build --prefix llvm-install  || die "Make step 3 failed"
    # Done with LLVM
    echo -e "Done with LLVM"
fi
#
export LLVM_ROOT=$PWD/llvm-install
export LLVM_CONFIG=$LLVM_ROOT/bin/llvm-config
#
echo -en "Build and Run first sample ? y|[n] > "
#
read REPLY
if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]; then
    echo -e "Skipping Sample"
else
    pushd Halide
        echo -e "Build samples, act 1"
        make tutorial_lesson_01_basics || die "Sample 1 - step 1 - failed"
        echo -e "Samples Act 1, done"
        #
        # Just run
        echo -e "First basic test (just run)"
        bin/tutorial_lesson_01_basics || die "Sample 1 - step 2 - failed"
    popd
fi
#
# Others:
# make tutorial_lesson_02_input_image
# make tutorial_lesson_03_debugging_1
# make tutorial_lesson_04_debugging_2
# make tutorial_lesson_05_scheduling_1
# make tutorial_lesson_06_realizing_over_shifted_domains
# make tutorial_lesson_07_multi_stage_pipelines
# ...
echo -e "--------------------------"
echo -e "Target list:"
for sample in $(ls Halide/tutorial/*.cpp); do
    bName=$(basename ${sample})
    echo "- tutorial_${bName%.cpp}"
done
#
echo -e "To run a target:"
echo -e "Make sure you have done:" 
echo -e " export LLVM_ROOT=$PWD/llvm-install"
echo -e " export LLVM_CONFIG=$LLVM_ROOT/bin/llvm-config"
echo -e "then:"
echo -e "  cd Halide"
echo -e "  make target-name"
#
echo -e "Done for now"
