package fi.nottingham.mobilefood.view;

import java.util.Date;
import java.util.List;

import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;

public interface IMainView {
	
	IMainViewPresenter getPresenter();

	void setFoods(Date selectedDate, List<Food> foods);
}
