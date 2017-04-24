This package is intended to allow automatic RMI compilation of
classes that use RMI. The idea is:

1.) You create the interface for the service here, calling it
    <name>.java

2.) You create the server side implementation here, calling it
    <name>Impl.java

3.) A rule has been added to the Ant task for this project so
    that the RMI stubs and skeletons for bgs.geophys.RMI.*Impl.class
    are automatically compiled. The rule (in build.xml) looks like this:
        <target name="-post-compile">
            <rmic base="${build.classes.dir}" includes="bgs/geophys/library/RMI/*Impl.class"/>
        </target>
