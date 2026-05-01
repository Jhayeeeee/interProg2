#!/bin/bash
# Compile the project
echo "Compiling University Clinic Appointment..."
mkdir -p out
javac -d out -cp "src:lib/*" src/*.java

# Run the project
if [ $? -eq 0 ]; then
    echo "Starting System..."
    java -cp "out:lib/*" ClinicAppointmentLogin
else
    echo "Compilation failed. Please check for errors."
fi
