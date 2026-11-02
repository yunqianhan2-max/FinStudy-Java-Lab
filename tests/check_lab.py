"""Verify displayed examples are the same sources used by the Android app.
Run: python tests/check_lab.py
"""
from pathlib import Path
import json,re,xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
main=root/'app/src/main'
entries=json.loads((main/'assets/lab/catalog.json').read_text(encoding='utf-8'))
assert len(entries)==10
assert len({e['key'] for e in entries})==10
catalog=(main/'java/com/finstudy/app/LabCatalog.java').read_text(encoding='utf-8')
knowledge=json.loads((main/'assets/knowledge.json').read_text(encoding='utf-8'))
knowledge_ids={k['knowledgeId'] for k in knowledge}
for entry in entries:
    key,layout,class_name=entry['key'],entry['layout'],entry['className']
    xml=(main/'res/layout'/(layout+'.xml')).read_bytes()
    java=(main/'java/com/finstudy/app'/(class_name+'.java')).read_bytes()
    assert xml==(main/'assets/lab'/(key+'.xml')).read_bytes(), 'displayed XML differs: '+key
    assert java==(main/'assets/lab'/(key+'.java')).read_bytes(), 'displayed Java differs: '+key
    assert 'R.layout.'+layout in catalog and 'new '+class_name+'()' in catalog
    assert entry['knowledge'] in knowledge_ids
    assert all(entry[k].strip() for k in ['goal','prediction','tryText'])
    # A binder must only look up IDs present in its own inflated layout.
    view_ids=set()
    for view in ET.fromstring(xml).iter():
        value=view.get('{http://schemas.android.com/apk/res/android}id','')
        if value.startswith('@+id/'):view_ids.add(value[5:])
    used_ids=set(re.findall(r'(?<!android\.)R\.id\.(\w+)',java.decode('utf-8')))
    assert used_ids<=view_ids, (key,used_ids-view_ids)
    assert ('class '+class_name+' implements LabExampleBinding') in java.decode('utf-8')
    assert 'public void bind(' in java.decode('utf-8')
print('PASS: all 10 XML/Java pairs match executable sources byte-for-byte')
print('PASS: each binder references only IDs in its own layout; catalog mappings and knowledge links exist')
