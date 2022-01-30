

1. install a recent jdk and configure the project to start in the jdk. (i tried with 1.8)
    -> i opened the project in intellij idea, then i changed the project sdk in project 
        structure (jdk 1.8 for me).

2. Move to the java_somtoolbox folder in terminal.
    -> i opened in the  intellij terminal .


3. execute - " sh somtoolbox.sh GrowingSOM som.prop ". (for windows somtoolbox.bat GrowingSOM som.prop) --> to train a GrowingSOM


4. This should get the training started.

For mac os, i had to make a line change in somtoolbox.sh as per the coding pdf given in tuwel.

somtoolbox.bat <applicationName>

### for visualization
somtoolbox.bat SOMViewer -u <unitDescriptionFile> -w <weightVectorFile> -v <inputVectorFile>
