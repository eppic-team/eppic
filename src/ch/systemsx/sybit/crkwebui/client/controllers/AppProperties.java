package ch.systemsx.sybit.crkwebui.client.controllers;

import com.google.gwt.i18n.client.Constants;

public interface AppProperties extends Constants 
{
	String login();
	String yes();
	String no();
	String input_email();
	String input_file();
	String input_advanced();
	String input_submit();

	String parameters_entropy();
	String parameters_kaks();
	String parameters_reduced_alphabet();
	String parameters_selecton();
	String parameters_allignment();
	String parameters_identity_cutoff();
	String parameters_use_tcoffee();
	String parameters_max_num_sequences();
	String parameters_others();
	String parameters_use_pisa();
	String parameters_asa_calc();
	String parameters_use_naccess();
	
	String status_panel_jobId();
	String status_panel_log();
	String status_panel_status();
	String status_panel_stop();
	
	String info_panel_pdb_identifier();
	String info_panel_total_core_size_xtal_call_cutoff();
	String info_panel_min_number_homologs_required();
	String info_panel_sequence_identity_cutoff();
	String info_panel_query_coverage_cutoff();
	String info_panel_per_member_core_size_xtal_call_cutoff();
	String info_panel_max_num_sequences_used();
	String info_panel_bio_call_cutoff();
	String info_panel_xtal_call_cutoff();
	String info_panel_download_results_link();
	
	String myjobs_panel_head();
	String myjobs_panel_new_button();
	String myjobs_grid_input();
	String myjobs_grid_status();
	
	String results_grid_details_button();
	String results_grid_details_button_tooltip();
	String results_grid_viewer_button();
	String results_grid_viewer_button_tooltip();
	
	String interfaces_residues_window();
	String interfaces_residues_panel_first_structure();
	String interfaces_residues_panel_second_structure();
	
	String scores_panel_column_weighted();
	String scores_panel_column_unweighted();
	String scores_panel_column_structure1();
	String scores_panel_column_structure2();
}
