$ErrorActionPreference = 'Stop'

function SqlLiteral([object]$value) {
    if ($null -eq $value) { return 'NULL' }
    return "'" + ([string]$value).Replace("'", "''") + "'"
}

function Add-ValuesInsert {
    param(
        [System.Collections.Generic.List[string]]$Lines,
        [string]$Table,
        [string[]]$Columns,
        [object[]]$Rows
    )

    $Lines.Add("INSERT INTO ``$Table``") | Out-Null
    $quotedColumns = $Columns | ForEach-Object { "``$_``" }
    $Lines.Add('(' + ($quotedColumns -join ', ') + ')') | Out-Null
    $Lines.Add('VALUES') | Out-Null

    if ($Rows.Count -gt 0 -and ($Rows[0] -is [string] -or $Rows[0] -is [ValueType])) {
        $formattedRow = $Rows | ForEach-Object { SqlLiteral $_ }
        $Lines.Add('(' + ($formattedRow -join ', ') + ');') | Out-Null
        $Lines.Add('') | Out-Null
        return
    }

    for ($i = 0; $i -lt $Rows.Count; $i++) {
        $suffix = if ($i -eq $Rows.Count - 1) { ';' } else { ',' }
        $formattedRow = $Rows[$i] | ForEach-Object { SqlLiteral $_ }
        $Lines.Add('(' + ($formattedRow -join ', ') + ')' + $suffix) | Out-Null
    }
    $Lines.Add('') | Out-Null
}

$students = @(
    @{ Name='Li Zihan'; Gender='female'; Relation='mother' },
    @{ Name='Wang Zixuan'; Gender='male'; Relation='father' },
    @{ Name='Zhang Yutong'; Gender='female'; Relation='mother' },
    @{ Name='Liu Haoran'; Gender='male'; Relation='mother' },
    @{ Name='Chen Yutong'; Gender='female'; Relation='mother' },
    @{ Name='Yang Yinuo'; Gender='female'; Relation='father' },
    @{ Name='Zhao Junxi'; Gender='male'; Relation='father' },
    @{ Name='Huang Siyuan'; Gender='male'; Relation='mother' },
    @{ Name='Zhou Ruoxi'; Gender='female'; Relation='mother' },
    @{ Name='Wu Jianing'; Gender='female'; Relation='father' },
    @{ Name='Xu Bowen'; Gender='male'; Relation='father' },
    @{ Name='Sun Xinyan'; Gender='female'; Relation='mother' },
    @{ Name='Zhu Yuhang'; Gender='male'; Relation='father' },
    @{ Name='Ma Kexin'; Gender='female'; Relation='mother' },
    @{ Name='Hu Chenxi'; Gender='female'; Relation='mother' },
    @{ Name='Guo Zichen'; Gender='male'; Relation='father' },
    @{ Name='He Shihan'; Gender='female'; Relation='mother' },
    @{ Name='Gao Jingxing'; Gender='male'; Relation='father' },
    @{ Name='Lin Zhiqing'; Gender='female'; Relation='mother' },
    @{ Name='Luo Yichen'; Gender='male'; Relation='father' },
    @{ Name='Zheng Yuxuan'; Gender='female'; Relation='mother' },
    @{ Name='Liang Zeyu'; Gender='male'; Relation='father' },
    @{ Name='Xie Anqi'; Gender='female'; Relation='mother' },
    @{ Name='Song Ruizhe'; Gender='male'; Relation='father' },
    @{ Name='Tang Kexin'; Gender='female'; Relation='mother' },
    @{ Name='Xu Muyang'; Gender='male'; Relation='father' },
    @{ Name='Han Xinyi'; Gender='female'; Relation='mother' },
    @{ Name='Feng Jiahao'; Gender='male'; Relation='father' },
    @{ Name='Deng Ruoning'; Gender='female'; Relation='mother' },
    @{ Name='Cao Zimo'; Gender='male'; Relation='father' },
    @{ Name='Peng Yilin'; Gender='female'; Relation='mother' },
    @{ Name='Zeng Haoxuan'; Gender='male'; Relation='father' },
    @{ Name='Xiao Yuchen'; Gender='female'; Relation='mother' },
    @{ Name='Tian Jiaxi'; Gender='male'; Relation='father' },
    @{ Name='Dong Shuyao'; Gender='female'; Relation='mother' },
    @{ Name='Yuan Qihang'; Gender='male'; Relation='father' },
    @{ Name='Pan Siyan'; Gender='female'; Relation='mother' },
    @{ Name='Yu Haoyu'; Gender='male'; Relation='father' },
    @{ Name='Yu Xinran'; Gender='female'; Relation='mother' },
    @{ Name='Du Chenyi'; Gender='male'; Relation='father' },
    @{ Name='Jiang Kexin'; Gender='female'; Relation='mother' },
    @{ Name='Wei Zihan'; Gender='male'; Relation='mother' },
    @{ Name='Cui Muchen'; Gender='male'; Relation='father' },
    @{ Name='Cheng Yunuo'; Gender='female'; Relation='mother' },
    @{ Name='Lu Jiayan'; Gender='male'; Relation='father' }
)

$teacherAccounts = @(
    @{ UserId=151101; ProfileId=541101; Name='Zhang Li'; TeacherNo='T31001'; Mobile='13966010001'; Gender='female'; Subject='math'; IsHead=1 },
    @{ UserId=151102; ProfileId=541102; Name='Liu Ying'; TeacherNo='T31002'; Mobile='13966010002'; Gender='female'; Subject='chinese'; IsHead=0 },
    @{ UserId=151103; ProfileId=541103; Name='Chen Fei'; TeacherNo='T31003'; Mobile='13966010003'; Gender='female'; Subject='english'; IsHead=0 }
)

$homeworks = @(
    @{ Id=560001; TeacherId=151101; Subject='math'; Title='Math Oral Practice 04-23'; Content='Finish oral arithmetic worksheet pages 12 to 13 and upload photos.'; Deadline='2026-04-24 20:00:00'; AllowLate=1; AllowResubmit=1; SubmitType='text,image'; NeedParent=0; Status='published'; PublishedAt='2026-04-23 16:30:00'; CreatedAt='2026-04-23 16:10:00'; UpdatedAt='2026-04-23 16:30:00'; Attachment='math-oral-practice.pdf' },
    @{ Id=560002; TeacherId=151102; Subject='chinese'; Title='Chinese Characters Unit 6'; Content='Copy Unit 6 characters twice and finish dictation.'; Deadline='2026-04-22 20:00:00'; AllowLate=1; AllowResubmit=1; SubmitType='text,image'; NeedParent=1; Status='published'; PublishedAt='2026-04-21 18:20:00'; CreatedAt='2026-04-21 18:00:00'; UpdatedAt='2026-04-21 18:20:00'; Attachment='unit-6-characters.pdf' },
    @{ Id=560003; TeacherId=151103; Subject='english'; Title='English Unit 5 Dictation'; Content='Complete Unit 5 dictation and upload the homework image.'; Deadline='2026-04-18 19:30:00'; AllowLate=1; AllowResubmit=0; SubmitType='image'; NeedParent=0; Status='closed'; PublishedAt='2026-04-17 16:40:00'; CreatedAt='2026-04-17 16:10:00'; UpdatedAt='2026-04-19 21:00:00'; Attachment='unit-5-word-list.pdf' },
    @{ Id=560004; TeacherId=151101; Subject='math'; Title='Weekend Math Draft'; Content='Draft for weekend challenge homework. No tasks generated.'; Deadline='2026-04-26 20:00:00'; AllowLate=1; AllowResubmit=1; SubmitType='text,file'; NeedParent=0; Status='draft'; PublishedAt=$null; CreatedAt='2026-04-23 18:10:00'; UpdatedAt='2026-04-23 18:10:00'; Attachment='weekend-math-draft.docx' },
    @{ Id=560005; TeacherId=151102; Subject='chinese'; Title='Poem Recitation Revoked'; Content='Originally planned as a recitation task and then revoked.'; Deadline='2026-04-25 18:00:00'; AllowLate=1; AllowResubmit=1; SubmitType='text,audio'; NeedParent=1; Status='revoked'; PublishedAt='2026-04-22 10:00:00'; CreatedAt='2026-04-22 09:30:00'; UpdatedAt='2026-04-22 14:00:00'; Attachment='poem-recitation-note.pdf' },
    @{ Id=560006; TeacherId=151101; Subject='math'; Title='Fraction Quiz In Class'; Content='Upload the corrected fraction quiz photo after class.'; Deadline='2026-04-23 12:00:00'; AllowLate=0; AllowResubmit=0; SubmitType='image'; NeedParent=0; Status='published'; PublishedAt='2026-04-23 10:20:00'; CreatedAt='2026-04-23 10:05:00'; UpdatedAt='2026-04-23 10:20:00'; Attachment='fraction-quiz.pdf' }
)

function Get-TaskMeta($homeworkId, $seq) {
    switch ($homeworkId) {
        560001 {
            if ($seq -le 18) { return @{ TaskStatus='completed'; HasSubmission=$true; HasReview=$true; IsLate=0; ReviewStatus='completed' } }
            if ($seq -le 28) { return @{ TaskStatus='submitted'; HasSubmission=$true; HasReview=$false; IsLate=0; ReviewStatus='unreviewed' } }
            if ($seq -le 34) { return @{ TaskStatus='revision_required'; HasSubmission=$true; HasReview=$true; IsLate=0; ReviewStatus='revision_required' } }
            return @{ TaskStatus='pending'; HasSubmission=$false; HasReview=$false; IsLate=0; ReviewStatus='unreviewed' }
        }
        560002 {
            if ($seq -le 18) { return @{ TaskStatus='completed'; HasSubmission=$true; HasReview=$true; IsLate=0; ReviewStatus='completed' } }
            if ($seq -le 23) { return @{ TaskStatus='completed'; HasSubmission=$true; HasReview=$true; IsLate=1; ReviewStatus='completed' } }
            if ($seq -le 28) { return @{ TaskStatus='submitted'; HasSubmission=$true; HasReview=$false; IsLate=1; ReviewStatus='unreviewed' } }
            if ($seq -le 35) { return @{ TaskStatus='revision_required'; HasSubmission=$true; HasReview=$true; IsLate=0; ReviewStatus='revision_required' } }
            return @{ TaskStatus='overdue'; HasSubmission=$false; HasReview=$false; IsLate=1; ReviewStatus='unreviewed' }
        }
        560003 {
            $late = if ($seq -ge 41) { 1 } else { 0 }
            return @{ TaskStatus='completed'; HasSubmission=$true; HasReview=$true; IsLate=$late; ReviewStatus='completed' }
        }
        560006 {
            if ($seq -le 10) { return @{ TaskStatus='completed'; HasSubmission=$true; HasReview=$true; IsLate=0; ReviewStatus='completed' } }
            if ($seq -le 20) { return @{ TaskStatus='submitted'; HasSubmission=$true; HasReview=$false; IsLate=0; ReviewStatus='unreviewed' } }
            return @{ TaskStatus='pending'; HasSubmission=$false; HasReview=$false; IsLate=0; ReviewStatus='unreviewed' }
        }
        default { throw "Unsupported homeworkId: $homeworkId" }
    }
}

function Get-SubmissionTime($homeworkId, $seq, $isLate) {
    switch ($homeworkId) {
        560001 { return ([datetime]'2026-04-23 18:10:00').AddMinutes($seq * 3) }
        560002 {
            if ($isLate -eq 1) {
                return ([datetime]'2026-04-23 08:00:00').AddMinutes($seq * 4)
            }
            return ([datetime]'2026-04-22 17:00:00').AddMinutes($seq * 3)
        }
        560003 {
            if ($isLate -eq 1) {
                return ([datetime]'2026-04-19 08:10:00').AddMinutes($seq * 2)
            }
            return ([datetime]'2026-04-18 16:40:00').AddMinutes($seq * 2)
        }
        560006 { return ([datetime]'2026-04-23 10:40:00').AddMinutes($seq * 2) }
    }
}

function Get-ReviewTime([datetime]$submittedAt, $homeworkId, $seq) {
    switch ($homeworkId) {
        560001 { return $submittedAt.AddMinutes(55 + ($seq % 5) * 7) }
        560002 { return $submittedAt.AddHours(8).AddMinutes($seq % 9) }
        560003 { return $submittedAt.AddHours(3).AddMinutes($seq % 6) }
        560006 { return $submittedAt.AddMinutes(35 + ($seq % 4) * 5) }
    }
}

function Get-Score($homeworkId, $seq, $reviewStatus) {
    if ($reviewStatus -eq 'revision_required') {
        return [decimal](68 + (($seq + $homeworkId) % 10))
    }
    switch ($homeworkId) {
        560001 { return [decimal](88 + ($seq % 10)) }
        560002 { return [decimal](82 + ($seq % 13)) }
        560003 { return [decimal](85 + ($seq % 12)) }
        560006 { return [decimal](80 + ($seq % 15)) }
    }
}

function Get-ScoreLevel([decimal]$score) {
    if ($score -ge 95) { return 'A+' }
    if ($score -ge 90) { return 'A' }
    if ($score -ge 80) { return 'B' }
    return 'C'
}

function Get-ReviewComment($reviewStatus, $subject, $seq) {
    if ($reviewStatus -eq 'revision_required') {
        switch ($subject) {
            'math' { return 'Please check the calculation steps carefully and resubmit.' }
            'chinese' { return 'Several characters need to be corrected and rewritten.' }
            default { return 'Please revise according to the feedback and submit again.' }
        }
    }
    switch ($subject) {
        'math' {
            if ($seq % 4 -eq 0) { return 'Work is clean and steps are clear.' }
            return 'Good accuracy, keep it up.'
        }
        'chinese' {
            if ($seq % 3 -eq 0) { return 'Handwriting is neat and dictation is mostly correct.' }
            return 'Character mastery is good. Please keep practicing.'
        }
        'english' {
            if ($seq % 5 -eq 0) { return 'Good dictation overall. Watch a few letters.' }
            return 'Vocabulary is good. Keep practicing pronunciation.'
        }
        default { return 'Completed well.' }
    }
}

$lines = [System.Collections.Generic.List[string]]::new()
$lines.Add('USE `primary_homework`;') | Out-Null
$lines.Add('') | Out-Null
$lines.Add('START TRANSACTION;') | Out-Null
$lines.Add('') | Out-Null
$lines.Add('-- Realistic single-school single-class test data') | Out-Null
$lines.Add('-- Prerequisite: run database init script first') | Out-Null
$lines.Add('-- Scope: 1 school, 1 class, 45 students, 45 parents, 3 teachers, 6 homeworks') | Out-Null
$lines.Add('-- Demo password: 123456') | Out-Null
$lines.Add('-- Sample login: admin school_admin_31 / teacher 13966010001 / student 30101 / parent 13866000001') | Out-Null
$lines.Add('') | Out-Null
$lines.Add('-- Cleanup old data for rerun safety') | Out-Null
$cleanupLines = @(
    'DELETE FROM `homework_review_asset` WHERE `id` BETWEEN 591001 AND 591500;',
    'DELETE FROM `homework_review` WHERE `homework_id` BETWEEN 560001 AND 560006;',
    'DELETE FROM `homework_submission_asset` WHERE `id` BETWEEN 581001 AND 581500;',
    'DELETE FROM `homework_submission` WHERE `homework_id` BETWEEN 560001 AND 560006;',
    'DELETE FROM `homework_task` WHERE `homework_id` BETWEEN 560001 AND 560006;',
    'DELETE FROM `homework_class` WHERE `homework_id` BETWEEN 560001 AND 560006;',
    'DELETE FROM `homework_attachment` WHERE `homework_id` BETWEEN 560001 AND 560006;',
    'DELETE FROM `operation_log` WHERE `id` BETWEEN 593001 AND 593500;',
    'DELETE FROM `notification` WHERE `id` BETWEEN 592001 AND 592500;',
    'DELETE FROM `homework` WHERE `id` BETWEEN 560001 AND 560006;',
    'DELETE FROM `user_parent_student` WHERE `id` BETWEEN 651001 AND 651045;',
    'DELETE FROM `user_teacher_class_subject` WHERE `id` BETWEEN 544001 AND 544003;',
    'DELETE FROM `user_parent` WHERE `id` BETWEEN 551001 AND 551045;',
    'DELETE FROM `user_student` WHERE `id` BETWEEN 451001 AND 451045;',
    'DELETE FROM `user_teacher` WHERE `id` BETWEEN 541101 AND 541103;',
    'DELETE FROM `user_account` WHERE `id` IN (151001,151101,151102,151103) OR (`id` BETWEEN 251001 AND 251045) OR (`id` BETWEEN 351001 AND 351045);',
    'DELETE FROM `school_class` WHERE `id` = 31101;',
    'DELETE FROM `school_grade` WHERE `id` = 311;',
    'DELETE FROM `school` WHERE `id` = 31;'
)
$cleanupLines | ForEach-Object { $lines.Add($_) | Out-Null }
$lines.Add('') | Out-Null

Add-ValuesInsert -Lines $lines -Table 'school' -Columns @('id','school_name','school_code','status','created_at','updated_at') -Rows @(
    @('31', 'Xinghe Experimental Primary School', 'SCH031', 'enabled', '2026-02-20 08:00:00', '2026-02-20 08:00:00')
)

Add-ValuesInsert -Lines $lines -Table 'school_grade' -Columns @('id','school_id','grade_name','school_year','status','created_at','updated_at') -Rows @(
    @('311', '31', 'Grade 3', '2025-2026', 'enabled', '2026-02-20 08:10:00', '2026-02-20 08:10:00')
)

$userAccountRows = [System.Collections.Generic.List[object]]::new()
$userAccountRows.Add(@('151001', 'school_admin_31', '123456', 'School Administrator', 'admin', '31', 'enabled', '2026-04-23 08:00:00', '2026-02-20 08:30:00', '2026-04-23 08:00:00')) | Out-Null
foreach ($teacher in $teacherAccounts) {
    $userAccountRows.Add(@([string]$teacher.UserId, $null, '123456', $teacher.Name, 'teacher', '31', 'enabled', '2026-04-23 07:50:00', '2026-02-20 09:00:00', '2026-04-23 07:50:00')) | Out-Null
}
for ($i = 0; $i -lt $students.Count; $i++) {
    $seq = $i + 1
    $studentUserId = 251000 + $seq
    $parentUserId = 351000 + $seq
    $createdAt = ([datetime]'2026-02-21 09:00:00').AddMinutes($seq)
    $updatedAt = ([datetime]'2026-04-23 19:00:00').AddMinutes($seq)
    $userAccountRows.Add(@([string]$studentUserId, $null, '123456', $students[$i].Name, 'student', '31', 'enabled', $updatedAt.ToString('yyyy-MM-dd HH:mm:ss'), $createdAt.ToString('yyyy-MM-dd HH:mm:ss'), $updatedAt.ToString('yyyy-MM-dd HH:mm:ss'))) | Out-Null
    $relationSuffix = if ($students[$i].Relation -eq 'father') { ' Dad' } else { ' Mom' }
    $parentName = $students[$i].Name + $relationSuffix
    $parentLoginAt = ([datetime]'2026-04-23 19:10:00').AddMinutes($seq)
    $userAccountRows.Add(@([string]$parentUserId, $null, '123456', $parentName, 'parent', '31', 'enabled', $parentLoginAt.ToString('yyyy-MM-dd HH:mm:ss'), $createdAt.ToString('yyyy-MM-dd HH:mm:ss'), $parentLoginAt.ToString('yyyy-MM-dd HH:mm:ss'))) | Out-Null
}
Add-ValuesInsert -Lines $lines -Table 'user_account' -Columns @('id','login_name','password_hash','user_name','role_type','school_id','status','last_login_at','created_at','updated_at') -Rows $userAccountRows

$teacherRows = [System.Collections.Generic.List[object]]::new()
foreach ($teacher in $teacherAccounts) {
    $teacherRows.Add(@([string]$teacher.ProfileId, [string]$teacher.UserId, '31', $teacher.TeacherNo, $teacher.Mobile, $teacher.Gender, '2026-02-20 09:05:00', '2026-02-20 09:05:00')) | Out-Null
}
Add-ValuesInsert -Lines $lines -Table 'user_teacher' -Columns @('id','teacher_user_id','school_id','teacher_no','mobile','gender','created_at','updated_at') -Rows $teacherRows

Add-ValuesInsert -Lines $lines -Table 'school_class' -Columns @('id','school_id','grade_id','class_name','class_code','homeroom_teacher_id','status','created_at','updated_at') -Rows @(
    @('31101','31','311','Grade 3 Class 1','SCH031-C301','151101','enabled','2026-02-20 09:20:00','2026-02-20 09:20:00')
)

$studentRows = [System.Collections.Generic.List[object]]::new()
$parentRows = [System.Collections.Generic.List[object]]::new()
$parentRelRows = [System.Collections.Generic.List[object]]::new()
for ($i = 0; $i -lt $students.Count; $i++) {
    $seq = $i + 1
    $student = $students[$i]
    $studentUserId = 251000 + $seq
    $studentId = 451000 + $seq
    $parentUserId = 351000 + $seq
    $parentId = 551000 + $seq
    $relationId = 651000 + $seq
    $studentNo = '{0:D5}' -f (30100 + $seq)
    $mobile = '13866{0:D6}' -f $seq
    $createdAt = ([datetime]'2026-02-21 09:00:00').AddMinutes($seq).ToString('yyyy-MM-dd HH:mm:ss')
    $studentRows.Add(@([string]$studentId,[string]$studentUserId,'31','311','31101',$studentNo,$student.Gender,'enabled',$createdAt,$createdAt)) | Out-Null
    $parentRows.Add(@([string]$parentId,[string]$parentUserId,'31',$mobile,$student.Gender,$createdAt,$createdAt)) | Out-Null
    $parentRelRows.Add(@([string]$relationId,[string]$parentUserId,[string]$studentId,$student.Relation,'1','enabled',$createdAt,$createdAt)) | Out-Null
}
Add-ValuesInsert -Lines $lines -Table 'user_student' -Columns @('id','student_user_id','school_id','grade_id','class_id','student_no','gender','status','created_at','updated_at') -Rows $studentRows
Add-ValuesInsert -Lines $lines -Table 'user_parent' -Columns @('id','parent_user_id','school_id','mobile','gender','created_at','updated_at') -Rows $parentRows
Add-ValuesInsert -Lines $lines -Table 'user_parent_student' -Columns @('id','parent_user_id','student_id','relation_type','is_primary','status','created_at','updated_at') -Rows $parentRelRows

Add-ValuesInsert -Lines $lines -Table 'user_teacher_class_subject' -Columns @('id','teacher_id','class_id','subject_code','is_head_teacher','status','created_at','updated_at') -Rows @(
    @('544001','151101','31101','math','1','enabled','2026-02-20 09:30:00','2026-02-20 09:30:00'),
    @('544002','151102','31101','chinese','0','enabled','2026-02-20 09:30:00','2026-02-20 09:30:00'),
    @('544003','151103','31101','english','0','enabled','2026-02-20 09:30:00','2026-02-20 09:30:00')
)

$homeworkRows = [System.Collections.Generic.List[object]]::new()
$homeworkAttachmentRows = [System.Collections.Generic.List[object]]::new()
$homeworkClassRows = [System.Collections.Generic.List[object]]::new()
for ($i = 0; $i -lt $homeworks.Count; $i++) {
    $hw = $homeworks[$i]
    $publishedAtValue = if ($hw.PublishedAt) { $hw.PublishedAt } else { $null }
    $homeworkRows.Add(@([string]$hw.Id,'31',[string]$hw.TeacherId,$hw.Subject,$hw.Title,$hw.Content,$hw.Deadline,[string]$hw.AllowLate,[string]$hw.AllowResubmit,$hw.SubmitType,[string]$hw.NeedParent,$hw.Status,$publishedAtValue,$hw.CreatedAt,$hw.UpdatedAt)) | Out-Null
    $assetId = 561001 + $i
    $assetUrl = "https://oss.example.com/real-school/homework/$($hw.Id)/attachment-$($hw.Id).pdf"
    $homeworkAttachmentRows.Add(@([string]$assetId,[string]$hw.Id,'file',$assetUrl,$hw.Attachment,[string](81234 + $i * 2345),'1',$hw.CreatedAt)) | Out-Null
    $classCreatedAt = if ($hw.PublishedAt) { $hw.PublishedAt } else { $hw.CreatedAt }
    $homeworkClassRows.Add(@([string](562001 + $i),[string]$hw.Id,'31101',$classCreatedAt)) | Out-Null
}
Add-ValuesInsert -Lines $lines -Table 'homework' -Columns @('id','school_id','creator_teacher_id','subject_code','title','content_text','deadline_at','allow_late_submit','allow_resubmit','submit_type_mask','need_parent_confirm','status','published_at','created_at','updated_at') -Rows $homeworkRows
Add-ValuesInsert -Lines $lines -Table 'homework_attachment' -Columns @('id','homework_id','asset_type','asset_url','asset_name','asset_size','sort_no','created_at') -Rows $homeworkAttachmentRows
Add-ValuesInsert -Lines $lines -Table 'homework_class' -Columns @('id','homework_id','class_id','created_at') -Rows $homeworkClassRows

$activeHomeworkIds = @(560001,560002,560003,560006)
$homeworkMap = @{}
foreach ($hw in $homeworks) { $homeworkMap[$hw.Id] = $hw }

$taskRows = [System.Collections.Generic.List[object]]::new()
$submissionRows = [System.Collections.Generic.List[object]]::new()
$submissionAssetRows = [System.Collections.Generic.List[object]]::new()
$reviewRows = [System.Collections.Generic.List[object]]::new()
$reviewAssetRows = [System.Collections.Generic.List[object]]::new()
$notifications = [System.Collections.Generic.List[object]]::new()
$operationLogs = [System.Collections.Generic.List[object]]::new()
$submissionSeq = 0
$reviewSeq = 0
$reviewAssetSeq = 0
$notificationSeq = 0

function New-NotificationId {
    param([ref]$Seq)
    $Seq.Value++
    return 592000 + $Seq.Value
}

foreach ($homeworkId in $activeHomeworkIds) {
    $hw = $homeworkMap[$homeworkId]
    for ($seq = 1; $seq -le $students.Count; $seq++) {
        $student = $students[$seq - 1]
        $meta = Get-TaskMeta -homeworkId $homeworkId -seq $seq
        $studentId = 451000 + $seq
        $studentUserId = 251000 + $seq
        $parentUserId = 351000 + $seq
        $studentNo = '{0:D5}' -f (30100 + $seq)
        $taskId = 570000 + $taskRows.Count + 1
        $taskCreatedAt = ([datetime]$hw.CreatedAt).AddMinutes(($seq % 7) + 1)
        $latestSubmissionId = $null
        $submissionCount = '0'
        $latestSubmittedAt = $null
        $latestReviewStatus = 'unreviewed'
        $latestReviewedAt = $null
        $taskUpdatedAt = $taskCreatedAt.ToString('yyyy-MM-dd HH:mm:ss')

        if ($meta.HasSubmission) {
            $submissionSeq++
            $submissionId = 580000 + $submissionSeq
            $submittedAt = Get-SubmissionTime -homeworkId $homeworkId -seq $seq -isLate $meta.IsLate
            $operatorRole = if ((($seq + $homeworkId) % 4) -eq 0) { 'parent' } else { 'student' }
            $operatorUserId = if ($operatorRole -eq 'parent') { $parentUserId } else { $studentUserId }
            $submitText = if ($operatorRole -eq 'parent') { 'Parent assisted with upload after finishing homework.' } else { 'Homework completed and submitted for review.' }
            $latestSubmissionId = [string]$submissionId
            $submissionCount = '1'
            $latestSubmittedAt = $submittedAt.ToString('yyyy-MM-dd HH:mm:ss')
            $taskUpdatedAt = $latestSubmittedAt
            $submissionRows.Add(@([string]$submissionId,[string]$taskId,[string]$homeworkId,[string]$studentId,$operatorRole,[string]$operatorUserId,$submitText,$submittedAt.ToString('yyyy-MM-dd HH:mm:ss'),[string]$meta.IsLate,'1','submitted',$submittedAt.ToString('yyyy-MM-dd HH:mm:ss'))) | Out-Null
            $submissionAssetRows.Add(@([string](581000 + $submissionAssetRows.Count + 1),[string]$submissionId,'image',("https://oss.example.com/real-school/submission/$homeworkId/$studentNo-1.jpg"),("$studentNo-$homeworkId.jpg"),[string](210000 + $seq * 13),'1',$submittedAt.ToString('yyyy-MM-dd HH:mm:ss'))) | Out-Null

            if ($meta.HasReview) {
                $reviewSeq++
                $reviewId = 590000 + $reviewSeq
                $reviewedAt = Get-ReviewTime -submittedAt $submittedAt -homeworkId $homeworkId -seq $seq
                $score = Get-Score -homeworkId $homeworkId -seq $seq -reviewStatus $meta.ReviewStatus
                $scoreLevel = Get-ScoreLevel -score $score
                $commentText = Get-ReviewComment -reviewStatus $meta.ReviewStatus -subject $hw.Subject -seq $seq
                $latestReviewStatus = $meta.ReviewStatus
                $latestReviewedAt = $reviewedAt.ToString('yyyy-MM-dd HH:mm:ss')
                $taskUpdatedAt = $latestReviewedAt
                $reviewRows.Add(@([string]$reviewId,[string]$taskId,[string]$homeworkId,[string]$studentId,[string]$submissionId,[string]$hw.TeacherId,$meta.ReviewStatus,([string]::Format('{0:0.00}', $score)),$scoreLevel,$commentText,$reviewedAt.ToString('yyyy-MM-dd HH:mm:ss'),$reviewedAt.ToString('yyyy-MM-dd HH:mm:ss'))) | Out-Null
                if ($meta.ReviewStatus -eq 'revision_required') {
                    $reviewAssetSeq++
                    $reviewAssetRows.Add(@([string](591000 + $reviewAssetSeq),[string]$reviewId,'image',("https://oss.example.com/real-school/review/$homeworkId/$studentNo-mark.png"),'1',$reviewedAt.ToString('yyyy-MM-dd HH:mm:ss'))) | Out-Null

                    $studentNoticeId = New-NotificationId -Seq ([ref]$notificationSeq)
                    $notifications.Add(@([string]$studentNoticeId,'review_result',[string]$reviewId,[string]$studentUserId,'student','in_app','Revision Required',("$($hw.Title) was reviewed. Please revise and resubmit."),'success',$reviewedAt.AddMinutes(2).ToString('yyyy-MM-dd HH:mm:ss'),$null,$reviewedAt.AddMinutes(2).ToString('yyyy-MM-dd HH:mm:ss'))) | Out-Null
                    $parentNoticeId = New-NotificationId -Seq ([ref]$notificationSeq)
                    $notifications.Add(@([string]$parentNoticeId,'review_result',[string]$reviewId,[string]$parentUserId,'parent','wechat','Child Homework Needs Revision',("$($student.Name) needs to revise $($hw.Title)."),'success',$reviewedAt.AddMinutes(3).ToString('yyyy-MM-dd HH:mm:ss'),$null,$reviewedAt.AddMinutes(3).ToString('yyyy-MM-dd HH:mm:ss'))) | Out-Null
                }
            }
        }

        if (($homeworkId -eq 560002) -and ($meta.TaskStatus -eq 'overdue')) {
            $reminderTime = ([datetime]'2026-04-23 08:30:00').AddMinutes($seq)
            $parentNoticeId = New-NotificationId -Seq ([ref]$notificationSeq)
            $notifications.Add(@([string]$parentNoticeId,'submission_reminder',[string]$homeworkId,[string]$parentUserId,'parent','wechat','Overdue Reminder',("$($student.Name) has not submitted $($hw.Title) yet."),'success',$reminderTime.ToString('yyyy-MM-dd HH:mm:ss'),$null,$reminderTime.ToString('yyyy-MM-dd HH:mm:ss'))) | Out-Null
        }

        $taskRows.Add(@([string]$taskId,[string]$homeworkId,[string]$studentId,'31101',$meta.TaskStatus,$latestSubmissionId,$submissionCount,$latestSubmittedAt,$latestReviewStatus,$latestReviewedAt,[string]$meta.IsLate,'0',$taskCreatedAt.ToString('yyyy-MM-dd HH:mm:ss'),$taskUpdatedAt)) | Out-Null
    }
}

foreach ($homeworkId in @(560001,560006)) {
    $hw = $homeworkMap[$homeworkId]
    $publishTime = [datetime]$hw.PublishedAt
    for ($seq = 1; $seq -le $students.Count; $seq++) {
        $student = $students[$seq - 1]
        $studentUserId = 251000 + $seq
        $parentUserId = 351000 + $seq
        $studentNoticeId = New-NotificationId -Seq ([ref]$notificationSeq)
        $notifications.Add(@([string]$studentNoticeId,'homework_publish',[string]$homeworkId,[string]$studentUserId,'student','in_app','New Homework',("$($hw.Title) has been published."),'success',$publishTime.AddMinutes($seq).ToString('yyyy-MM-dd HH:mm:ss'),$null,$publishTime.AddMinutes($seq).ToString('yyyy-MM-dd HH:mm:ss'))) | Out-Null
        $parentNoticeId = New-NotificationId -Seq ([ref]$notificationSeq)
        $notifications.Add(@([string]$parentNoticeId,'homework_publish',[string]$homeworkId,[string]$parentUserId,'parent','wechat','Child Has New Homework',("$($student.Name) received new homework: $($hw.Title)."),'success',$publishTime.AddMinutes($seq).AddSeconds(30).ToString('yyyy-MM-dd HH:mm:ss'),$null,$publishTime.AddMinutes($seq).AddSeconds(30).ToString('yyyy-MM-dd HH:mm:ss'))) | Out-Null
    }
}

Add-ValuesInsert -Lines $lines -Table 'homework_task' -Columns @('id','homework_id','student_id','class_id','task_status','latest_submission_id','submission_count','latest_submitted_at','latest_review_status','latest_reviewed_at','is_late','is_deleted','created_at','updated_at') -Rows $taskRows
Add-ValuesInsert -Lines $lines -Table 'homework_submission' -Columns @('id','task_id','homework_id','student_id','operator_role','operator_user_id','submit_text','submitted_at','is_late','version_no','submit_status','created_at') -Rows $submissionRows
Add-ValuesInsert -Lines $lines -Table 'homework_submission_asset' -Columns @('id','submission_id','asset_type','asset_url','asset_name','asset_size','sort_no','created_at') -Rows $submissionAssetRows
Add-ValuesInsert -Lines $lines -Table 'homework_review' -Columns @('id','task_id','homework_id','student_id','submission_id','reviewer_teacher_id','review_status','score','score_level','comment_text','reviewed_at','created_at') -Rows $reviewRows
if ($reviewAssetRows.Count -gt 0) {
    Add-ValuesInsert -Lines $lines -Table 'homework_review_asset' -Columns @('id','review_id','asset_type','asset_url','sort_no','created_at') -Rows $reviewAssetRows
}
if ($notifications.Count -gt 0) {
    Add-ValuesInsert -Lines $lines -Table 'notification' -Columns @('id','biz_type','biz_id','receiver_user_id','receiver_role','notify_channel','notify_title','notify_content','send_status','sent_at','read_at','created_at') -Rows $notifications
}

$operationLogs.Add(@('593001','151101','teacher','homework','560001','publish_homework','{"title":"Math Oral Practice 04-23","classId":31101}','0','2026-04-23 16:30:00')) | Out-Null
$operationLogs.Add(@('593002','151102','teacher','homework','560002','publish_homework','{"title":"Chinese Characters Unit 6","classId":31101}','0','2026-04-21 18:20:00')) | Out-Null
$operationLogs.Add(@('593003','151103','teacher','homework','560003','close_homework','{"title":"English Unit 5 Dictation","classId":31101}','0','2026-04-19 21:00:00')) | Out-Null
$operationLogs.Add(@('593004','151101','teacher','homework','560004','save_homework_draft','{"title":"Weekend Math Draft"}','0','2026-04-23 18:10:00')) | Out-Null
$operationLogs.Add(@('593005','151102','teacher','homework','560005','revoke_homework','{"title":"Poem Recitation Revoked"}','0','2026-04-22 14:00:00')) | Out-Null
$operationLogs.Add(@('593006','151101','teacher','homework','560006','publish_homework','{"title":"Fraction Quiz In Class","classId":31101}','0','2026-04-23 10:20:00')) | Out-Null
$operationLogs.Add(@('593007','251001','student','submission','580001','submit_homework','{"taskId":570001,"assetCount":1}','0','2026-04-23 18:13:00')) | Out-Null
$operationLogs.Add(@('593008','351004','parent','submission','580004','submit_homework_for_child','{"taskId":570004,"assetCount":1}','0','2026-04-23 18:22:00')) | Out-Null
$operationLogs.Add(@('593009','151101','teacher','review','590019','review_homework','{"taskId":570029,"reviewStatus":"revision_required"}','0','2026-04-23 20:45:00')) | Out-Null
$operationLogs.Add(@('593010','151102','teacher','notification','592201','remind_pending_homework','{"homeworkId":560002,"classId":31101,"remindType":"overdue"}','0','2026-04-23 08:40:00')) | Out-Null
Add-ValuesInsert -Lines $lines -Table 'operation_log' -Columns @('id','operator_user_id','operator_role','biz_type','biz_id','action_type','request_payload','result_code','created_at') -Rows $operationLogs

$lines.Add('COMMIT;') | Out-Null
$lines.Add('') | Out-Null

$target = Join-Path $PSScriptRoot '..\docs\product\primary-school-homework\real-school-class-test-data.sql'
$target = [System.IO.Path]::GetFullPath($target)
$lines | Set-Content -Path $target -Encoding UTF8

Write-Output ("Generated: {0}" -f $target)
Write-Output ("Students: {0}" -f $students.Count)
Write-Output ("Homeworks: {0}" -f $homeworks.Count)
Write-Output ("Tasks: {0}" -f $taskRows.Count)
Write-Output ("Submissions: {0}" -f $submissionRows.Count)
Write-Output ("Reviews: {0}" -f $reviewRows.Count)
Write-Output ("Notifications: {0}" -f $notifications.Count)

