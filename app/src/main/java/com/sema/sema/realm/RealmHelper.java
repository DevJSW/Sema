package com.sema.sema.realm;

import com.sema.sema.realm.models.Tag;

import java.lang.reflect.Array;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.exceptions.RealmException;

/**
 * Created by Shephard on 8/6/2017.
 */

public class RealmHelper
{

    Realm realm;
    RealmResults<Tag> tags;
    Boolean Saved = null;

    public RealmHelper(Realm realm)
    {
        this.realm = realm;
    }

    //write or save
    public boolean save(final Tag tag)
    {

        if (tag == null) {

            Saved = false;
        } else {

            realm.executeTransaction(new Realm.Transaction()
            {
                @Override
                public void execute(Realm realm)
                {
                    try {
                        Tag t = realm.copyToRealm(tag);
                        Saved = true;
                    } catch (RealmException e)
                    {

                        e.printStackTrace();
                        Saved = false;
                    }
                }
            });
        }

        return  Saved;
    }

    // RETRIEVE FROM REALM DATABASE
    public  void retrieveFromRealmDB()
    {

        tags = realm.where(Tag.class).findAll();
    }

    //JUST REFRESH
    public ArrayList<Tag> justRefresh()
    {

        ArrayList<Tag> latest = new ArrayList<>();
        for (Tag t : tags)
        {
            latest.add(t);
        }

        return latest;
    }
}
