package ch.systemsx.sybit.crkwebui.client.commons.appdata;

import com.google.gwt.i18n.client.Constants;

/**
 * Client side properties.
 * @author srebniak_a
 *
 */
public interface AppProperties extends Constants 
{
	String login();
	String defaultmask();
	String yes();
	String no();
	String option_true();
	String option_false();
	
	String window_title();
	String window_title_input();
	String window_title_help();
	String window_title_downloads();
	String window_title_releases();
	String window_title_processing();
	String window_title_loading();
	String window_title_results();
	
	String top_panel_title();
	
	String downloads_panel_download_crk_link();
	String downloads_panel_download_crk_link_hint();
	
	String input_title();
	String input_email();
	String input_file();
	String input_example();
	String input_uniprot_version();
	String input_example_hint();
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
	String input_citation();
	String input_citation_link_tooltip();
	String input_citation_link_text();
	String input_news_header();

	String parameters_entropy();
	String parameters_geometry();
	String parameters_reduced_alphabet();
	String parameters_reduced_alphabet_hint();
	String parameters_allignment();
	String parameters_soft_identity_cutoff();
	String parameters_soft_identity_cutoff_hint();
	String parameters_hard_identity_cutoff();
	String parameters_hard_identity_cutoff_hint();
	String parameters_max_num_sequences();
	String parameters_search_mode();
	String parameters_search_mode_hint();
	String parameters_others();
	String parameters_max_num_sequences_hint();
	String parameters_use_naccess_hint();
	
	String status_panel_jobId();
	String status_panel_log();
	String status_panel_status();
	String status_panel_stop();
	String status_panel_step_counter();
	String status_panel_subtitle();
	
	String info_panel_pdb_identifier();
	String info_panel_input_parameters();
	String info_panel_input_parameters_hint();
	String info_panel_download_results_link();
	String info_panel_download_results_link_hint();
	String info_panel_homologs_info();
	String info_panel_general_info();
	String info_panel_uniprot();
	String info_panel_crk();
	String info_panel_experiment();
	String info_panel_resolution();
	String info_panel_rfree();
	String info_panel_spacegroup();
	String info_panel_nothing_found();
	
	String pdb_identifier_panel_label_hint();
	
	String pdb_identifier_panel_warning_lowRes_hint();
	String pdb_identifier_panel_warning_lowRes();
	String pdb_identifier_panel_warning_highRfree_hint();
	String pdb_identifier_panel_warning_highRfree();

	String homologs_panel_chains_hint();
	String homologs_panel_uniprot_hint();
	String homologs_panel_uniprot_no_query_match_hint();
	String homologs_panel_nrhomologs_hint();
	String homologs_panel_entropiespse_hint();
	String homologs_panel_query_warnings_title();
	String homologs_panel_next_homologs_button();
	String homologs_panel_prev_homologs_button();

	
	String myjobs_panel_head();
	String myjobs_panel_new_button();
	String myjobs_grid_stop_tooltip();
	String myjobs_grid_delete_tooltip();
	
	String results_grid_empty_text();
	String results_grid_details_button();
	String results_grid_details_button_tooltip();
	String results_grid_viewer_button();
	String results_grid_viewer_button_tooltip();
	String results_grid_viewer_combo_label();
	String results_grid_show_thumbnails();
	String results_grid_thumbnail_tooltip_text();
	String results_grid_warnings_tooltip_title();
	
	String alignment_window_title();
	
	String interfaces_residues_window_title();
	String interfaces_residues_panel_structure();
	String interfaces_residues_combo_all();
	String interfaces_residues_combo_rimcore();
	String interfaces_residues_combo_title();
	
	String interfaces_residues_summary_corerim_heading();
	String interfaces_residues_summary_corerim_avgcore();
	String interfaces_residues_summary_corerim_avgcore_hint();
	String interfaces_residues_summary_corerim_avgrim();
	String interfaces_residues_summary_corerim_avgrim_hint();
	String interfaces_residues_summary_corerim_final();
	String interfaces_residues_summary_corerim_final_hint();

	String interfaces_residues_summary_coresurface_heading();
	String interfaces_residues_summary_coresurface_mean();
	String interfaces_residues_summary_coresurface_mean_hint();
	String interfaces_residues_summary_coresurface_sd();
	String interfaces_residues_summary_coresurface_sd_hint();
	String interfaces_residues_summary_coresurface_final();
	String interfaces_residues_summary_coresurface_final_hint();

	String interfaces_residues_summary_sizes_heading();
	String interfaces_residues_summary_sizes_cores();
	String interfaces_residues_summary_sizes_cores_hint();
	String interfaces_residues_summary_sizes_rims();
	String interfaces_residues_summary_sizes_rims_hint();
	
	String bottom_panel_home_link_label();
	String bottom_panel_about_link_label();
	String bottom_panel_help_link_label();
	String bottom_panel_downloads_link_label();
	String bottom_panel_releases_link_label();
	String bottom_panel_contact_link();
	String bottom_panel_contact_link_label();
	String bottom_panel_status_error_refresh_page();
	
	String about_window_title();
	
	String error_message_box_header();
	String waiting_message_box_header();
	String waiting_message_box_info();
	
	String callback_get_xsrf_token_error();
	String callback_get_current_status_data();
	String callback_get_interface_residues_error();
	String callback_get_jobs_for_current_session_error();
	String callback_get_jobs_for_current_session_ok();
	String callback_get_jobs_for_current_session_changed();
	String callback_get_results_of_processing_error();
	String callback_get_settings_error();
	String callback_delete_job_error();
	String callback_delete_job_message();
	String callback_stop_job_error();
	String callback_stop_job_message();
	String callback_run_job_error();
	String callback_untie_jobs_from_session_error();
	String callback_job_not_found_error();
	
	String viewer_jmol();
	String viewer_local();
	String viewer_pse();
}
