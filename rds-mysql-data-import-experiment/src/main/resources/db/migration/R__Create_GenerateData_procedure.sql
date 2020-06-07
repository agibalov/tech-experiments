drop procedure if exists GenerateData;

create procedure GenerateData(in numberOfSchools int, in numberOfClasses int, in numberOfStudents int)
begin
    declare schoolIndex int default 0;
    declare schoolCount int default 0;
    declare schoolId varchar(64);

    declare classIndex int default 0;
    declare classCount int default 0;
    declare classId varchar(64);

    declare studentIndex int default 0;
    declare studentCount int default 0;
    declare studentId varchar(64);

    while schoolIndex <= numberOfSchools do
        set schoolId = concat('school', schoolCount);
        insert into Schools(id, name)
        values (schoolId, concat('School ', schoolCount));
        set schoolIndex = schoolIndex + 1;
        set schoolCount = schoolCount + 1;

        set classIndex = 0;
        while classIndex <= numberOfClasses do
            set classId = concat('class', classCount);
            insert into Classes(id, schoolId, name)
            values (classId, schoolId, concat('Class ', classCount));
            set classIndex = classIndex + 1;
            set classCount = classCount + 1;

            set studentIndex = 0;
            while studentIndex <= numberOfStudents do
                set studentId = concat('student', studentCount);
                insert into Students(id, classId, name)
                values (studentId, classId, concat('Student ', studentCount));
                set studentIndex = studentIndex + 1;
                set studentCount = studentCount + 1;
            end while;
        end while;
    end while;
end;
