FROM fedora:38
LABEL maintainer="Mirko Sciachero <m.sciachero@gmail.com>"
RUN dnf -y install rpm-build fontconfig mesa-libGL &&  \
    dnf clean all
RUN curl -s "https://get.sdkman.io" | bash
RUN source "/root/.sdkman/bin/sdkman-init.sh" && \
    yes | sdk install java 17.0.8-tem && \
    sdk flush


