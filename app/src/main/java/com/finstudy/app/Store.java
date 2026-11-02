package com.finstudy.app;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import org.json.*;
import java.util.*;
public class Store extends SQLiteOpenHelper   {
    public Store(Context c)  {
        super(c,"finstudy.db",null,1);
    }
    public void onCreate(SQLiteDatabase d)  {
        d.execSQL("CREATE TABLE progress(id TEXT PRIMARY KEY,status INTEGER NOT NULL)");
        d.execSQL("CREATE TABLE favorite(id TEXT PRIMARY KEY)");
        d.execSQL("CREATE TABLE attempt(id INTEGER PRIMARY KEY AUTOINCREMENT,token TEXT UNIQUE,chapter INTEGER,correct INTEGER,total INTEGER,at INTEGER,kind TEXT)");
        d.execSQL("CREATE TABLE answer(attempt INTEGER,question TEXT,user TEXT,correct INTEGER,PRIMARY KEY(attempt,question))");
        d.execSQL("CREATE TABLE wrong(id TEXT PRIMARY KEY,count INTEGER,mastered INTEGER,at INTEGER)");
    }
    public void onUpgrade(SQLiteDatabase d,int o,int n)  {
    }
    public int scalar(String sql,String... args)  {
        try(Cursor c=getReadableDatabase().rawQuery(sql,args))  {
            return c.moveToFirst()?c.getInt(0):0;
        }
    }
    public int status(String id)  {
        return scalar("SELECT status FROM progress WHERE id=?",id);
    }
    public void status(String id,int s)  {
        ContentValues v=new ContentValues();
        v.put("id",id);
        v.put("status",s);
        getWritableDatabase().insertWithOnConflict("progress",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }
    public boolean favorite(String id)  {
        return scalar("SELECT COUNT(*) FROM favorite WHERE id=?",id)>0;
    }
    public void toggle(String id)  {
        if(favorite(id))getWritableDatabase().delete("favorite","id=?",new String[]  {
            id
        }
        );
        else  {
            ContentValues v=new ContentValues();
            v.put("id",id);
            getWritableDatabase().insertOrThrow("favorite",null,v);
        }
    }
    public void mastered(String id,boolean m)  {
        ContentValues v=new ContentValues();
        v.put("mastered",m?1:0);
        getWritableDatabase().update("wrong",v,"id=?",new String[]  {
            id
        }
        );
    }
    /** 事务保证成绩、逐题答案与错题一起保存；token 防止重复提交。 */
    public long submit(String token,int chapter,ArrayList<JSONObject> qs,ArrayList<String> answers,boolean review)  {
        if (qs.isEmpty() || qs.size() != answers.size()) throw new IllegalArgumentException("题目与答案数量不一致");
        SQLiteDatabase d=getWritableDatabase();
        d.beginTransaction();
        try  {
            try(Cursor c=d.rawQuery("SELECT id FROM attempt WHERE token=?",new String[]  {
                token
            }
            ))  {
                if(c.moveToFirst())  {
                    long id=c.getLong(0);
                    d.setTransactionSuccessful();
                    return id;
                }
            }
            int correct=0;
            for(int i=0;i<qs.size();i++)if(Grading.matches(Repo.answer(qs.get(i).optJSONArray("correctAnswers")), answers.get(i)))correct++;
            ContentValues a=new ContentValues();
            a.put("token",token);
            a.put("chapter",chapter);
            a.put("correct",correct);
            a.put("total",qs.size());
            a.put("at",System.currentTimeMillis());
            a.put("kind",review?"review":"chapter");
            long id=d.insertOrThrow("attempt",null,a);
            for(int i=0;i<qs.size();i++)  {
                JSONObject q=qs.get(i);
                String qid=q.optString("questionId");
                boolean ok=Grading.matches(Repo.answer(q.optJSONArray("correctAnswers")), answers.get(i));
                ContentValues v=new ContentValues();
                v.put("attempt",id);
                v.put("question",qid);
                v.put("user",answers.get(i));
                v.put("correct",ok?1:0);
                d.insertOrThrow("answer",null,v);
                if(!ok)  {
                    d.execSQL("INSERT OR IGNORE INTO wrong(id,count,mastered,at) VALUES(?,0,0,0)",new Object[]  {
                        qid
                    }
                    );
                    d.execSQL("UPDATE wrong SET count=count+1,mastered=0,at=? WHERE id=?",new Object[]  {
                        System.currentTimeMillis(),qid
                    }
                    );
                }
            }
            d.setTransactionSuccessful();
            return id;
        }
        finally  {
            d.endTransaction();
        }
    }
    public int completed(Repo r)  {
        int done=0;
        for (JSONObject chapter : r.availableChapters(false)) {
            int ch = chapter.optInt("chapterId");
            ArrayList<JSONObject> list=r.items(r.knowledge,ch);
            boolean all=!list.isEmpty();
            for(JSONObject k:list)if(status(k.optString("knowledgeId"))!=2)all=false;
            if(all)done++;
        }
        return done;
    }
}
