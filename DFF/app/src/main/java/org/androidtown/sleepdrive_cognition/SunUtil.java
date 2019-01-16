package org.androidtown.sleepdrive_cognition;

import java.io.File;

import android.os.Environment;

public class SunUtil {
    // 디렉토리를 만든다.
    public static String makeDir(String dirName)
    {
        String mRootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + dirName;

        try
        {
            File fRoot = new File(mRootPath);
            if (fRoot.exists() == false)
            {
                if (fRoot.mkdirs() == false)
                {
                    throw new Exception("");
                }
            }
        }
        catch (Exception e)
        {
            mRootPath = "-1";
        }

        return mRootPath + "/";
    }
}
