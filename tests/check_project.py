"""Run: python tests/check_project.py . Tests source data and actual SQLite constraints.
It does not claim Android UI or device testing."""
from pathlib import Path
import json,re,sqlite3,subprocess,os,tempfile,xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
main=root/'app/src/main'
load=lambda name: json.loads((main/'assets'/name).read_text(encoding='utf-8'))
chapters,knowledge,questions=load('chapters.json'),load('knowledge.json'),load('questions.json')
assert len(chapters)==5 and all(c['available'] for c in chapters)
assert len(knowledge)==34 and len(questions)==50
for items,key in [(chapters,'chapterId'),(knowledge,'knowledgeId'),(questions,'questionId')]:
    assert len({x[key] for x in items})==len(items)
for chapter in chapters:
    qs=[q for q in questions if q['chapterId']==chapter['chapterId']]
    assert len(qs)==10 and sum(q['type']=='single' for q in qs)==7
    assert any(k['chapterId']==chapter['chapterId'] for k in knowledge)
for q in questions:
    assert len(q['options'])==4 and q['explanation'].strip()
    a=q['correctAnswers'];assert a and len(set(a))==len(a) and all(0<=x<4 for x in a)
    assert (q['type']=='single' and len(a)==1) or (q['type']=='multiple' and len(a)>=2)
legacy=json.loads((root/'tests/legacy-ids.json').read_text())
assert set(legacy['knowledgeId'])<={x['knowledgeId'] for x in knowledge}
assert set(legacy['questionId'])<={x['questionId'] for x in questions}
assert not list(root.rglob('*.kt')) and not list(root.rglob('*.kts'))
for p in (main/'java').rglob('*.java'):
    assert '->' not in p.read_text(encoding='utf-8'), 'Use course-style listeners: '+str(p)
android='{http://schemas.android.com/apk/res/android}'
resources={p.stem for p in (main/'res/layout').glob('*.xml')}
ids=set()
for p in (main/'res').rglob('*.xml'):
    document=ET.parse(p)
    for e in document.iter():
        value=e.get(android+'id','')
        if value.startswith('@+id/'):ids.add(value[5:])
        if e.tag=='include':assert e.attrib['layout'].split('/')[-1] in resources
for p in (main/'java').rglob('*.java'):
    source=p.read_text(encoding='utf-8')
    assert set(re.findall(r'(?<!android\.)R\.id\.(\w+)',source))<=ids, p
    assert set(re.findall(r'(?<!android\.)R\.layout\.(\w+)',source))<=resources, p
manifest=ET.parse(main/'AndroidManifest.xml')
activities=manifest.findall('./application/activity')
assert len(activities)==12
for activity in activities:
    name=activity.attrib[android+'name'].split('.')[-1]
    assert (main/'java/com/finstudy/app'/(name+'.java')).exists()
assert not manifest.findall('uses-permission')
print('PASS: 5 chapters, 34 knowledge points, 50 questions, legacy IDs, Java-only source, 12 activities, XML references')
# Compare schemas without changing or clearing any real app database.
source=(main/'java/com/finstudy/app/Store.java').read_text(encoding='utf-8')
schema=re.findall(r'd\.execSQL\("(CREATE TABLE [^"]+)"\)',source)
assert len(schema)==5
old=sqlite3.connect(':memory:');old.executescript((root/'tests/legacy-schema.sql').read_text())
new=sqlite3.connect(':memory:')
for sql in schema:new.execute(sql)
for name in ['progress','favorite','attempt','answer','wrong']:
    assert old.execute('PRAGMA table_info('+name+')').fetchall()==new.execute('PRAGMA table_info('+name+')').fetchall()
new.execute("INSERT INTO progress VALUES('c1k1',2)")
new.execute("INSERT INTO favorite VALUES('c1k1')")
new.execute("INSERT INTO attempt(token,chapter,correct,total,at,kind) VALUES('one',1,8,10,0,'chapter')")
try:
    new.execute("INSERT INTO attempt(token) VALUES('one')")
    raise AssertionError('duplicate token accepted')
except sqlite3.IntegrityError:pass
for _ in range(2):
    new.execute('INSERT OR IGNORE INTO wrong(id,count,mastered,at) VALUES(?,0,0,0)',('c1q01',))
    new.execute('UPDATE wrong SET count=count+1,mastered=0,at=? WHERE id=?',(1,'c1q01'))
assert new.execute('SELECT COUNT(*) FROM wrong').fetchone()[0]==1
assert new.execute('SELECT count FROM wrong').fetchone()[0]==2
new.execute("UPDATE wrong SET mastered=1 WHERE id='c1q01'")
new.execute('UPDATE wrong SET count=count+1,mastered=0,at=? WHERE id=?',(2,'c1q01'))
assert new.execute('SELECT count,mastered FROM wrong').fetchone()==(3,0)
new.commit();new.execute('BEGIN')
new.execute("INSERT INTO attempt(token) VALUES('rollback')")
new.rollback()
assert new.execute("SELECT COUNT(*) FROM attempt WHERE token='rollback'").fetchone()[0]==0
assert new.execute("SELECT status FROM progress WHERE id='c1k1'").fetchone()[0]==2
print('PASS: unchanged legacy table layouts, unique token, deduplicated wrong questions, repeated errors, rollback')
# Compile and run the real Grading.java independently from Android.
java_home=os.environ.get('JAVA_HOME')
if not java_home:
    for candidate in [Path('D:/Android/openjdk/jdk-17.0.12'),Path('D:/Android/AndroidStudio/jbr')]:
        if (candidate/'bin/javac.exe').exists():java_home=str(candidate);break
javac=str(Path(java_home)/'bin/javac.exe') if java_home else 'javac'
java=str(Path(java_home)/'bin/java.exe') if java_home else 'java'
output=root/'build/course-tests'
output.mkdir(parents=True,exist_ok=True)
subprocess.run([javac,'-encoding','UTF-8','-d',str(output),str(main/'java/com/finstudy/app/Grading.java'),str(root/'tests/GradingTest.java')],check=True)
subprocess.run([java,'-cp',str(output),'GradingTest'],check=True)
