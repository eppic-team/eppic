package ch.systemsx.sybit.crkwebui.client.controllers;

import com.google.gwt.i18n.client.Constants;

/**
 * Client side properties
 * @author srebniak_a
 *
 */
public interface AppProperties extends Constants 
{
	String login();
	String yes();
	String no();
	String option_true();
	String option_false();
	
	String input_email();
	String input_file();
	String input_advanced();
	String input_submit();
	String input_submit_error();
	String input_submit_waiting_message();
	String input_submit_form_invalid_header();
	String input_submit_form_invalid_message();
	String input_submit_form_no_methods_selected();
	String input_reset();
	String input_upload_file_radio();
	String input_pdb_code_radio();
	String input_pdb_input_type();

	String parameters_entropy();
	String parameters_kaks();
	String parameters_geometry();
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
	String parameters_use_tcoffee_hint();
	String parameters_use_tcoffee_fast();
	String parameters_use_tcoffee_normal();
	String parameters_max_num_sequences_hint();
	String parameters_asa_calc_hint();
	String parameters_use_naccess_hint();
	
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
	String results_grid_viewer_combo_label();
	String results_grid_show_thumbnails();
	String results_grid_thumbnail_tooltip_text();
	String results_grid_warnings_tooltip_title();
	
	String interfaces_residues_window_title();
	String interfaces_residues_panel_first_structure();
	String interfaces_residues_panel_second_structure();
	String interfaces_residues_combo_all();
	String interfaces_residues_combo_rimcore();
	String interfaces_residues_combo_title();
	String interfaces_residues_aggergation_total_cores();
	String interfaces_residues_aggergation_total_rims();
	String interfaces_residues_aggergation_ratios();
	
	String scores_panel_column_weighted();
	String scores_panel_column_unweighted();
	String scores_panel_column_structure1();
	String scores_panel_column_structure2();
	
	String bottom_panel_contact_link();
	String bottom_panel_contact_link_label();
	String bottom_panel_status_error_refresh_page();
	
	String error_message_box_header();
	String waiting_message_box_header();
	String waiting_message_box_info();
	
	String callback_get_current_status_data();
	String callback_get_interface_residues_error();
	String callback_get_jobs_for_current_session_error();
	String callback_get_results_of_processing_error();
	String callback_get_settings_error();
	String callback_kill_job_error();
	String callback_run_job_error();
	String callback_untie_jobs_from_session_error();
	String callback_job_not_found_error();
	
	String legend_panel_names();
	String legend_panel_styles();
	
	String viewer_jmol();
	String viewer_local();
}
