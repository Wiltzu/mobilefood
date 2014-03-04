/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.nottingham.mobilefood;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Application;
import android.util.Log;
import dagger.ObjectGraph;
import fi.nottingham.mobilefood.service.IFileSystemService;

public class DaggerApplication extends Application implements IFileSystemService {
	private ObjectGraph graph;

	@Override
	public void onCreate() {
		super.onCreate();

		AndroidModule androidModule = new AndroidModule();
		androidModule.setDaggerApplication(this);
		graph = ObjectGraph.create(androidModule);
	}

	public void inject(Object object) {
		graph.inject(object);
	}

	ObjectGraph getApplicationGraph() {
		return graph;
	}
	
	@Override
	public InputStream openInputFile(String filename) throws FileNotFoundException{
		return openFileInput(filename);
	}
	
	@Override
	public OutputStream openOutputFile(String filename) {
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = openFileOutput(filename, MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			//TODO: improve error handling!!!
			Log.e("DaggerApplication", "Unexpected error occured while opening FileOutputStream", e);
		}
		return fileOutputStream;
	}
}
